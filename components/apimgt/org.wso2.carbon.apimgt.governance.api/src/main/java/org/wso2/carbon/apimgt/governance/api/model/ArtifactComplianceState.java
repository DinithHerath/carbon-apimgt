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

package org.wso2.carbon.apimgt.governance.api.model;

/**
 * This class represents the compliance state of an artifact
 */
public enum ArtifactComplianceState {
    COMPLIANT,
    NON_COMPLIANT,
    NOT_APPLICABLE;

    public static ArtifactComplianceState fromString(String text) {
        if ("compliant".equalsIgnoreCase(text)) {
            return COMPLIANT;
        } else if ("non_compliant".equalsIgnoreCase(text)) {
            return NON_COMPLIANT;
        } else if ("not_applicable".equalsIgnoreCase(text)) {
            return NOT_APPLICABLE;
        }
        return NOT_APPLICABLE;
    }
}
