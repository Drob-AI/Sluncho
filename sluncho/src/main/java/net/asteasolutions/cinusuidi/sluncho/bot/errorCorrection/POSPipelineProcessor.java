/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.asteasolutions.cinusuidi.sluncho.bot.errorCorrection;

import java.util.ArrayList;
import java.util.List;
import net.asteasolutions.cinusuidi.sluncho.bot.CompositeQuery;
import net.asteasolutions.cinusuidi.sluncho.bot.Query;
import net.asteasolutions.cinusuidi.sluncho.bot.postPipelineProcessors.IPostPipelineProcessor;
import net.asteasolutions.cinusuidi.sluncho.questionparser.QueryToken;
import net.asteasolutions.cinusuidi.sluncho.questionparser.helpers.GateOffsetBoundary;

/**
 *
 * @author mihail
 */
public class POSPipelineProcessor implements IPostPipelineProcessor {
    String[] subjectCandidateTypes = new String[]{
        "PRP","PP", "NP", "NPS", "NN", "NNS", "NNP", "NNPS"
    };
    
    String[] predicateCandidateTypes = new String[]{
        "VBZ", "VB", "VBG", "VBP", "VBD", "VBN"
    };

    public List<QueryToken> getTokensSublist(List<QueryToken> tokens, Long start, Long end) {
        List<QueryToken> result = new ArrayList<QueryToken>();
        for(QueryToken token: tokens) {
            if(token.getBoundary().end < start) {
                continue;
            } else if (token.getBoundary().start > end) {
                break;
            } else {
                result.add(token);
            }
        }
        return result;
    }
    
    public List<QueryToken> getPosSublist(Query q, Long start, Long end) {
        return getTokensSublist(q.posTokens, start, end);
    }
    
    public List<Long> getBeginings(List<QueryToken> tokens) {
        List<Long> result = new ArrayList(tokens.size());
        int idx = 0;
        for(QueryToken token: tokens) {
            result.set(idx, token.getBoundary().start);
            idx++;
        }
        return result;
    }
    
    public QueryToken tryGet(List<QueryToken> tokens, String type, Integer idx, boolean direction) {
        if (direction) {
            for(int i = tokens.size() - 1; i >= 0; i--) {
                QueryToken token = tokens.get(i);
                if(token.getAnnotations().get("category") == null ? type == null : token.getAnnotations().get("category").equals(type)) {
                    idx = i;
                    return token;
                }
            }
        } else {
            for(int i = 0; i < tokens.size(); i++) {
                QueryToken token = tokens.get(i);
                if(token.getAnnotations().get("category") == null ? type == null : token.getAnnotations().get("category").equals(type)) {
                    idx = i;
                    return token;
                }
            }
        }
        idx = -1;
        return null;
    }
    
    public QueryToken tryGetOneOf(List<QueryToken> tokens, String[] types, Integer idx, boolean direction) {
        for(String type: types) {
            QueryToken t = tryGet(tokens, type, idx, direction);
            if(t != null) {
                return t;
            }
        }
        return null;
    }
    
    public QueryToken tryExtractSubject(Query q) {
        for(QueryToken t: q.orderedTokens) {
            if("true".equals(t.getAnnotations().get("Subject"))) {
                return t;
            }
        }
        return null;
    }

    public Long annotateSubject(Query q, List<QueryToken> subjectTokens) {
        Integer result = -1;
        q.subject = q.subjectToken;

        if(q.subject == null) {
            q.subject = tryGetOneOf(subjectTokens, subjectCandidateTypes, result, false);
        }
        if(q.subject == null) {
            q.subject = tryGetOneOf(q.posTokens, subjectCandidateTypes, result, true);
        }
        if(q.subject == null) {
            if(subjectTokens.size() >= 4) {
                q.subject = subjectTokens.get(3);
            }
        }
        if(q.subject == null) {
            if(subjectTokens.size() >= 2) {
                q.subject = subjectTokens.get(1);
            }
        }
        if(q.subject == null) {
            if(subjectTokens.size() >= 1) {
                q.subject = subjectTokens.get(0);
            }
        }
        return q.subject.getBoundary().end;
    }
    
    public Integer annotatePredicate(Query q, List<QueryToken> subjectTokens) {
        Integer result = -1;
        q.predicate = tryGetOneOf(subjectTokens, predicateCandidateTypes, result, true);
        if(q.predicate == null) {
            q.predicate = tryGetOneOf(q.posTokens, predicateCandidateTypes, result, true);
        }
        if(q.predicate == null) {
            if(subjectTokens.size() >= 2) {
                q.predicate = subjectTokens.get(1);
            }     
        }
        if(q.predicate == null) {
            if(subjectTokens.size() >= 1) {
                q.predicate = subjectTokens.get(0);
            }
        }
        return result;
    }
    
    public Query annotateNounPhrase(Query q) {
        QueryToken largest = null;
        Long lengthiest = 0L;
        for(QueryToken t: q.allNPTokens) {
            Long newLen = t.getBoundary().length;
            if(lengthiest < newLen) {
                lengthiest = newLen;
                largest = t;
            }
        }
        q.nounPhrase = largest;
        return q;
    }
    
    public void lastResortPredicateExtraction(Query q) {
        Integer i = 0;
        if(q.predicate == null) {
            QueryToken firstToken = null;
            if(q.posTokens.size() >= 1) {
                q.posTokens.get(0);
            }
            if(firstToken != null) {
                String firstTokenType = firstToken.getAnnotations().get("category");
                if(firstTokenType != null){ 
                    if(firstTokenType.equals("WP$")) {
                       q.predicate = tryGetOneOf(q.posTokens, predicateCandidateTypes, i, true);
                       if(q.predicate != null) {
                            q.predicateGroup.add(q.predicate);
                       }
                    }
                    if (firstTokenType.equals("WP") || firstTokenType.equals("WRB")){
                        QueryToken verb1 = tryGetOneOf(q.posTokens, predicateCandidateTypes, i, true);
                        if(verb1 != null) {
                            q.predicateGroup.add(verb1);
                            QueryToken last = q.posTokens.get(q.posTokens.size() - 1);
                            if (last != null) {
                                List<QueryToken> rest = getPosSublist(q, new Long(verb1.getBoundary().end + 1), last.getBoundary().end);
                                QueryToken verb2 = tryGetOneOf(rest, predicateCandidateTypes, i, true);
                                if (verb2 != null) {
                                    q.predicate = verb2;
                                    q.predicateGroup.add(verb2);
                                }
                            }
                            if(q.predicate == null) {
                                q.predicate = verb1;
                            }
                        }
                    }
                }
            }
            q.predicate = tryGetOneOf(q.posTokens, predicateCandidateTypes,i,true);
            if(q.predicate != null) {
                q.predicateGroup.add(q.predicate);
            }
        }
        
        if(q.predicate == null) {
            if(q.allNPTokens.size() >= 1) {
                q.predicate = q.allNPTokens.get(0);
                q.predicateGroup.add(q.predicate);
            }
        };
    } 
    
    public Query simpleAnnotate(Query q) {
        List<QueryToken> subjectTokens = q.posTokens;
        boolean hasGateSubjectTokens = false;
        if(q.subjectToken != null) {
            GateOffsetBoundary boundry = q.subjectToken.getBoundary();
            subjectTokens = getPosSublist(q, boundry.start, boundry.end);
            hasGateSubjectTokens = true;
        }
        QueryToken subjectGroupElement = tryExtractSubject(q); 
        if(subjectGroupElement != null) {
            GateOffsetBoundary subjectGroupBoundry = subjectGroupElement.getBoundary();
            List<QueryToken> subjectGroupTokens = getPosSublist(q, subjectGroupBoundry.start, subjectGroupBoundry.end);
            q.subjectGroup = subjectGroupTokens;
            Integer i = 0;
            q.subject = tryGetOneOf(subjectGroupTokens, subjectCandidateTypes, i, false);
            if(q.subject == null) {
                q.subject = subjectGroupTokens.get(subjectGroupTokens.size() - 1);
            }
        } else {
            Long end = annotateSubject(q, subjectTokens); //q.subject is defined
            QueryToken largestContainer = null;
            List<QueryToken> validNPTs = new ArrayList<>();
            for(QueryToken t: q.allNPTokens) {
                GateOffsetBoundary b = t.getBoundary();
                if(b.end <= end) {
                    validNPTs.add(t);
                }
            }
            List<QueryToken> bestNounGroup = null;
            int lenghtiestNounGroup = -1;
            for(QueryToken t: validNPTs) {
                List<QueryToken> nounGroup = getPosSublist(q, t.getBoundary().start, end);
                if(nounGroup.size() > lenghtiestNounGroup) {
                    lenghtiestNounGroup = nounGroup.size();
                    bestNounGroup = nounGroup;
                }
            }
            if(bestNounGroup != null) {
                q.subjectGroup = bestNounGroup;
            } else {
                q.subjectGroup = new ArrayList<>();
                q.subjectGroup.add(q.subject);
            }
            
//            q.predicateGroup = new ArrayList<QueryToken>();
//            lastResortPredicateExtraction(q);
        }
        
        Integer i = 0;
        q.predicateGroup = new ArrayList<QueryToken>();
        if(hasGateSubjectTokens) {
            q.predicateGroup = filterTypes(subjectTokens, predicateCandidateTypes);
            q.predicate = tryGetOneOf(q.predicateGroup, predicateCandidateTypes, i, true);
        }
        if(q.predicate == null) {
            lastResortPredicateExtraction(q);
        }        
        
        if(q.subject != null) {
            Long subjectStart = q.subject.getBoundary().start;
            Long subjectEnd = q.subject.getBoundary().end;
            
            List<QueryToken> validNPTs = new ArrayList<>();
            for(QueryToken t: q.allNPTokens) {
                GateOffsetBoundary b = t.getBoundary();
                
                if(subjectStart >= b.start && subjectEnd <= b.end) {
                } else {
                    validNPTs.add(t);
                }
            }
            if(validNPTs.isEmpty()) {
                validNPTs = q.allNPTokens;
            }
            List<QueryToken> bestNounGroup = null;
            int lenghtiestNounGroup = -1;
            for(QueryToken t: validNPTs) {
                List<QueryToken> nounGroup = getPosSublist(q, t.getBoundary().start, t.getBoundary().end);
                if(nounGroup.size() > lenghtiestNounGroup) {
                    lenghtiestNounGroup = nounGroup.size();
                    bestNounGroup = nounGroup;
                }
            }
            q.additionGroup = bestNounGroup;
            if(q.additionGroup == null) {
                q.additionGroup = new ArrayList<>();
            }
        }
        
//        annotatePredicate(q, subjectTokens);
//        annotateNounPhrase(q);
        return q;
    }

    @Override
    public ArrayList<CompositeQuery> expand(ArrayList<CompositeQuery> queries) {
        for(CompositeQuery q: queries) {
            simpleAnnotate(q);
            for(Query sentence: q.sentences) {
                simpleAnnotate(sentence);
            }
        }
        return queries;
    }

    private List<QueryToken> filterTypes(List<QueryToken> subjectTokens, String[] predicateCandidateTypes) {
        ArrayList<QueryToken> result = new ArrayList<>();
        for(QueryToken t: subjectTokens) {
            String annotation = t.getAnnotations().get("category");
            for(String type: predicateCandidateTypes) {
                if(annotation.equals(type)){
                    result.add(t);
                    break;
                }
            }
        }
        
        return result;
    }
    
}
