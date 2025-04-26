package org.oaebudt.edc.report.model;

import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.PolicyType;

import java.util.Arrays;

import static org.eclipse.edc.policy.model.OdrlNamespace.ODRL_SCHEMA;

public enum OaebudtPolicyType {

    DATA_PROCESSOR("require-dataprocessor", PolicyType.SET, ODRL_SCHEMA + "use",
            "DataAccess.level", Operator.EQ, "processing"),
    SENSITIVE("require-sensitive", PolicyType.SET, ODRL_SCHEMA + "use",
            "DataAccess.level", Operator.EQ, "sensitive");

    private String id;
    private PolicyType type;
    private String action;
    private String leftOperand;
    private Operator operator;
    private String rightOperand;

    OaebudtPolicyType(String id, PolicyType type, String action, String leftOperand, Operator operator, String rightOperand) {
        this.id = id;
        this.type = type;
        this.action = action;
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;
    }

    public static OaebudtPolicyType getById(String id) {
        return Arrays.stream(OaebudtPolicyType.values())
                .filter(policyType -> policyType.id.equalsIgnoreCase(id))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Policy id not found"));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PolicyType getType() {
        return type;
    }

    public void setType(PolicyType type) {
        this.type = type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getLeftOperand() {
        return leftOperand;
    }

    public void setLeftOperand(String leftOperand) {
        this.leftOperand = leftOperand;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public String getRightOperand() {
        return rightOperand;
    }

    public void setRightOperand(String rightOperand) {
        this.rightOperand = rightOperand;
    }
}
