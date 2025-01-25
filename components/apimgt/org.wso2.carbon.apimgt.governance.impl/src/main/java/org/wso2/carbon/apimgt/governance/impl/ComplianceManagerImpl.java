/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.ComplianceManager;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.ComplianceEvaluationResult;
import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.PolicyAdherenceSate;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Severity;
import org.wso2.carbon.apimgt.governance.impl.dao.ComplianceMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.GovernancePolicyMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.RulesetMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.ComplianceMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.GovernancePolicyMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.RulesetMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.util.APIMUtil;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the Compliance Manager, which is responsible for managing compliance related operations
 */
public class ComplianceManagerImpl implements ComplianceManager {
    private static final Log log = LogFactory.getLog(ComplianceManagerImpl.class);

    private final ComplianceMgtDAO complianceMgtDAO;
    private final GovernancePolicyMgtDAO governancePolicyMgtDAO;

    private final RulesetMgtDAO rulesetMgtDAO;

    public ComplianceManagerImpl() {
        complianceMgtDAO = ComplianceMgtDAOImpl.getInstance();
        governancePolicyMgtDAO = GovernancePolicyMgtDAOImpl.getInstance();
        rulesetMgtDAO = RulesetMgtDAOImpl.getInstance();
    }


    /**
     * Handle Policy Change Event
     *
     * @param policyId     Policy ID
     * @param organization Organization
     */
    @Override
    public void handlePolicyChangeEvent(String policyId, String organization) throws GovernanceException {

        // Get the policy and its labels and associated governable states

        GovernancePolicy policy = governancePolicyMgtDAO.getGovernancePolicyByID(policyId);
        List<String> labels = policy.getLabels();

        List<GovernableState> governableStates = policy.getGovernableStates();

        // Get artifacts that should be governed by the policy
        List<ArtifactInfo> artifacts = new ArrayList<>();
        artifacts.addAll(getArtifactsByLabelsAndGovernableState(labels, governableStates));

        for (ArtifactInfo artifact : artifacts) {
            String artifactId = artifact.getArtifactId();
            ArtifactType artifactType = artifact.getArtifactType();
            complianceMgtDAO.addComplianceEvaluationRequest(artifactId, artifactType,
                    policyId, organization);
        }
    }

    /**
     * Get Artifacts by Labels and Governable State
     *
     * @param labels           List of labels
     * @param governableStates List of governable states
     * @return Map of artifact ID and artifact type
     */
    private List<ArtifactInfo> getArtifactsByLabelsAndGovernableState(List<String> labels,
                                                                      List<GovernableState> governableStates) {
        List<ArtifactInfo> artifactInfoList = new ArrayList<>();
        List<String> correspondingAPIStates =
                APIMUtil.getCorrespondingAPIStatusesForGovernableStates(governableStates);

        // Get API Artifacts
        for (String label : labels) {
            // TODO: Get artifacts by label, Filter APIs from state, Get artifact type from APIM
        }
        return artifactInfoList;
    }

    /**
     * Handle Ruleset Change Event
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     */
    @Override
    public void handleRulesetChangeEvent(String rulesetId, String organization) throws GovernanceException {
        List<String> policies = rulesetMgtDAO.getAssociatedPoliciesForRuleset(rulesetId);

        for (String policyId : policies) {
            handlePolicyChangeEvent(policyId, organization);
        }
    }

    /**
     * Handle API Compliance Evaluation Request Async
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param govPolicies  List of governance policies to be evaluated
     * @param organization Organization
     * @throws GovernanceException If an error occurs while handling the API compliance evaluation
     */
    @Override
    public void handleComplianceEvaluationAsync(String artifactId, ArtifactType artifactType,
                                                List<String> govPolicies,
                                                String organization) throws GovernanceException {

        for (String policyId : govPolicies) {
            complianceMgtDAO.addComplianceEvaluationRequest(artifactId, artifactType,
                    policyId, organization);
        }

    }

    /**
     * Get Rule Violations
     *
     * @param artifactId Artifact ID
     * @param policyId   Policy ID
     * @param rulesetId  Ruleset ID
     * @return List of Rule Violations
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    @Override
    public List<RuleViolation> getRuleViolations(String artifactId, String policyId, String rulesetId)
            throws GovernanceException {
        return complianceMgtDAO.getRuleViolations(artifactId, policyId, rulesetId);
    }

    /**
     * Get the rule violations by artifact ID based on severity
     *
     * @param artifactId Artifact ID
     * @return Map of Rule Violations based on severity
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    @Override
    public Map<Severity, List<RuleViolation>> getSeverityBasedRuleViolationsForArtifact(String artifactId)
            throws GovernanceException {
        List<RuleViolation> ruleViolations = complianceMgtDAO.getRuleViolationsByArtifactId(artifactId);
        Map<Severity, List<RuleViolation>> severityBasedRuleViolations = new HashMap<>();
        for (RuleViolation ruleViolation : ruleViolations) {
            Severity severity = ruleViolation.getSeverity();
            if (severityBasedRuleViolations.containsKey(severity)) {
                severityBasedRuleViolations.get(severity).add(ruleViolation);
            } else {
                List<RuleViolation> ruleViolationList = new ArrayList<>();
                ruleViolationList.add(ruleViolation);
                severityBasedRuleViolations.put(severity, ruleViolationList);
            }
        }
        return severityBasedRuleViolations;
    }

    /**
     * Get Compliance Evaluation Result
     *
     * @param artifactId Artifact ID
     * @param policyId   Policy ID
     * @param rulesetId  Ruleset ID
     * @return Compliance Evaluation Result
     * @throws GovernanceException If an error occurs while getting the compliance evaluation result
     */
    @Override
    public ComplianceEvaluationResult getComplianceEvaluationResult(String artifactId,
                                                                    String policyId, String rulesetId)
            throws GovernanceException {
        return complianceMgtDAO.getComplianceEvaluationResult(artifactId, policyId, rulesetId);
    }

    /**
     * Get list of evaluated policies by artifact ID
     *
     * @param artifactId Artifact ID
     * @return List of evaluated policy IDs
     * @throws GovernanceException If an error occurs while getting the list of evaluated policies
     */
    @Override
    public List<String> getEvaluatedPoliciesByArtifactId(String artifactId) throws GovernanceException {
        List<ComplianceEvaluationResult> complianceEvaluationResults =
                complianceMgtDAO.getComplianceEvaluationResultsByArtifactId(artifactId);
        Set<String> evaluatedPolicies = new HashSet<>();
        for (ComplianceEvaluationResult complianceEvaluationResult : complianceEvaluationResults) {
            evaluatedPolicies.add(complianceEvaluationResult.getPolicyId());
        }
        return new ArrayList<>(evaluatedPolicies);
    }

    /**
     * Get list of evaluated rulesets by artifact ID and policy ID
     *
     * @param artifactId Artifact ID
     * @param policyId   Policy ID
     * @return List of evaluated ruleset IDs
     * @throws GovernanceException If an error occurs while getting the list of evaluated rulesets
     */
    @Override
    public List<String> getEvaluatedRulesetsByArtifactIdAndPolicyId(String artifactId, String policyId)
            throws GovernanceException {
        List<ComplianceEvaluationResult> complianceEvaluationResults =
                complianceMgtDAO.getComplianceEvaluationResultsByArtifactAndPolicyId(artifactId, policyId);
        Set<String> evaluatedRulesets = new HashSet<>();
        for (ComplianceEvaluationResult complianceEvaluationResult : complianceEvaluationResults) {
            evaluatedRulesets.add(complianceEvaluationResult.getRulesetId());
        }
        return new ArrayList<>(evaluatedRulesets);

    }

    /**
     * Get a map of compliant and non-compliant artifacts
     *
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return Map of compliant and non-compliant artifacts
     * @throws GovernanceException If an error occurs while getting the compliant and non-compliant artifacts
     */
    @Override
    public Map<ArtifactComplianceState, List<String>> getComplianceStateOfEvaluatedArtifacts(
            ArtifactType artifactType, String organization) throws GovernanceException {
        List<String> allComplianceEvaluatedArtifacts =
                complianceMgtDAO.getAllComplianceEvaluatedArtifacts(artifactType, organization);
        List<String> nonCompliantArtifacts = complianceMgtDAO.getNonCompliantArtifacts(artifactType, organization);

        Map<ArtifactComplianceState, List<String>> compliantAndNonCompliantArtifacts = new HashMap<>();
        compliantAndNonCompliantArtifacts.put(ArtifactComplianceState.COMPLIANT, new ArrayList<>());
        compliantAndNonCompliantArtifacts.put(ArtifactComplianceState.NON_COMPLIANT, new ArrayList<>());

        for (String artifact : allComplianceEvaluatedArtifacts) {
            if (nonCompliantArtifacts.contains(artifact)) {
                compliantAndNonCompliantArtifacts.get(ArtifactComplianceState.NON_COMPLIANT).add(artifact);
            } else {
                compliantAndNonCompliantArtifacts.get(ArtifactComplianceState.COMPLIANT).add(artifact);
            }
        }

        return compliantAndNonCompliantArtifacts;
    }

    /**
     * Get a map of policies followed and violated in the organization
     *
     * @param organization Organization
     * @return Map of policies followed and violated
     * @throws GovernanceException If an error occurs while getting the policy adherence
     */
    @Override
    public Map<PolicyAdherenceSate, List<String>> getAdherenceStateofEvaluatedPolicies(String organization)
            throws GovernanceException {
        List<String> allComplianceEvaluatedPolicies = complianceMgtDAO.getAllComplianceEvaluatedPolicies(organization);
        List<String> nonCompliantPolicies = complianceMgtDAO.getViolatedPolicies(organization);

        Map<PolicyAdherenceSate, List<String>> policyAdherence = new HashMap<>();
        policyAdherence.put(PolicyAdherenceSate.FOLLOWED, new ArrayList<>());
        policyAdherence.put(PolicyAdherenceSate.VIOLATED, new ArrayList<>());

        for (String policy : allComplianceEvaluatedPolicies) {
            if (nonCompliantPolicies.contains(policy)) {
                policyAdherence.get(PolicyAdherenceSate.VIOLATED).add(policy);
            } else {
                policyAdherence.get(PolicyAdherenceSate.FOLLOWED).add(policy);
            }
        }

        return policyAdherence;
    }

    /**
     * Get a map of artifacts evaluated by policy
     *
     * @param policyId            Policy ID
     * @param resolveArtifactName Whether the artifact name should be resolved
     * @return Map of artifacts evaluated by policy
     * @throws GovernanceException If an error occurs while getting the artifacts evaluated by policy
     */
    @Override
    public Map<ArtifactComplianceState, List<ArtifactInfo>> getComplianceStateOfEvaluatedArtifactsByPolicy
    (String policyId, boolean resolveArtifactName) throws GovernanceException {

        Map<ArtifactType, List<ComplianceEvaluationResult>> complianceEvaluationResults =
                complianceMgtDAO.getEvaluationResultsForPolicy(policyId);

        Map<ArtifactComplianceState, List<ArtifactInfo>> complianceStateOfEvaluatedArtifacts = new HashMap<>();

        complianceStateOfEvaluatedArtifacts.put(ArtifactComplianceState.COMPLIANT, new ArrayList<>());
        complianceStateOfEvaluatedArtifacts.put(ArtifactComplianceState.NON_COMPLIANT, new ArrayList<>());

        for (ArtifactType artifactType : complianceEvaluationResults.keySet()) {
            List<ComplianceEvaluationResult> evaluationResults = complianceEvaluationResults.get(artifactType);
            Set<String> allEvaluatedArtifacts = new HashSet<>();
            Set<String> nonCompliantArtifacts = new HashSet<>();

            for (ComplianceEvaluationResult evaluationResult : evaluationResults) {
                String artifactId = evaluationResult.getArtifactId();
                boolean isEvaluationFailed = evaluationResult.isEvaluationSuccess();
                allEvaluatedArtifacts.add(artifactId);
                if (isEvaluationFailed) {
                    nonCompliantArtifacts.add(artifactId);
                }
            }

            for (String artifactId : allEvaluatedArtifacts) {
                ArtifactInfo artifactInfo = new ArtifactInfo();
                artifactInfo.setArtifactId(artifactId);
                artifactInfo.setArtifactType(artifactType);
                if (resolveArtifactName) {
                    artifactInfo.setDisplayName(GovernanceUtil.getArtifactName(artifactId, artifactType));
                }
                if (nonCompliantArtifacts.contains(artifactId)) {
                    complianceStateOfEvaluatedArtifacts.get(ArtifactComplianceState.NON_COMPLIANT).add(artifactInfo);
                } else {
                    complianceStateOfEvaluatedArtifacts.get(ArtifactComplianceState.COMPLIANT).add(artifactInfo);
                }
            }
        }

        return complianceStateOfEvaluatedArtifacts;
    }
}
