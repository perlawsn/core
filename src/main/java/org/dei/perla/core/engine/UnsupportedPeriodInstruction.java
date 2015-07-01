package org.dei.perla.core.engine;

/**
 * A special error {@link Instruction} for notifying that the selected
 * sampling frequency is not supported by the end device.
 *
 * @author Guido Rota 30/06/15.
 */
public class UnsupportedPeriodInstruction implements Instruction {

    private final static String period = "${param['period']}";

    private final String suggestedExpr;

    public UnsupportedPeriodInstruction(String suggestedExpr) {
        this.suggestedExpr = suggestedExpr;
    }

    protected String getSuggestedExpr() {
        return suggestedExpr;
    }

    @Override
    public void setNext(Instruction instruction) throws IllegalStateException {}

    @Override
    public Instruction next() {
        return null;
    }

    @Override
    public Instruction run(Runner runner) throws ScriptException {
        Long unsupported = Executor.evaluateExpression(runner.ctx, period,
                Long.class);
        Long suggested = Executor.evaluateExpression(runner.ctx, suggestedExpr,
                Long.class);

        if (suggested == null) {
            throw new ScriptException(
                    "Unsupported sampling period " + unsupported);
        } else {
            throw new UnsupportedPeriodException(unsupported, suggested);
        }
    }

}
