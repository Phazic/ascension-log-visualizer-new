package net.java.swingfx.waitwithstyle;

import java.awt.event.ActionListener;

/**
 * A progress panel that is cancelable instead of infinite. The work is done by
 * the CancelableAdaptee. Allows programatic cancelling (maybe useful for
 * timeouts), and adding a listener to the user's cancel so that the action the
 * user is waiting on is stopped or ignored.
 *
 * @author Michael Bushe michael@bushe.com
 */
public class CancelableProgressPanel extends InfiniteProgressPanel {
    /**
     *
     */
    private static final long serialVersionUID = 4989345408598546359L;

    public CancelableProgressPanel() {
        super();
        this.setupAdapter();
    }

    public CancelableProgressPanel(final String text) {
        super(text);
        this.setupAdapter();
    }

    public CancelableProgressPanel(final String text, final int barsCount) {
        super(text, barsCount);
        this.setupAdapter();
    }

    public CancelableProgressPanel(final String text, final int barsCount,
            final float shield) {
        super(text, barsCount, shield);
        this.setupAdapter();
    }

    public CancelableProgressPanel(final String text, final int barsCount,
            final float shield, final float fps) {
        super(text, barsCount, shield, fps);
        this.setupAdapter();
    }

    public CancelableProgressPanel(final String text, final int barsCount,
            final float shield, final float fps, final int rampDelay) {
        super(text, barsCount, shield, fps, rampDelay);
        this.setupAdapter();
    }

    public CancelableProgressPanel(final CancelableProgessAdapter adapter) {
        super();
        this.infiniteProgressAdapter = adapter;
    }

    public CancelableProgressPanel(final String text,
            final CancelableProgessAdapter adapter) {
        super(text);
        this.infiniteProgressAdapter = adapter;
    }

    public CancelableProgressPanel(final String text, final int barsCount,
            final CancelableProgessAdapter adapter) {
        super(text, barsCount);
        this.infiniteProgressAdapter = adapter;
    }

    public CancelableProgressPanel(final String text, final int barsCount,
            final float shield, final CancelableProgessAdapter adapter) {
        super(text, barsCount, shield);
        this.infiniteProgressAdapter = adapter;
    }

    public CancelableProgressPanel(final String text, final int barsCount,
            final float shield, final float fps,
            final CancelableProgessAdapter adapter) {
        super(text, barsCount, shield, fps);
        this.infiniteProgressAdapter = adapter;
    }

    public CancelableProgressPanel(final String text, final int barsCount,
            final float shield, final float fps, final int rampDelay,
            final CancelableProgessAdapter adapter) {
        super(text, barsCount, shield, fps, rampDelay);
        this.infiniteProgressAdapter = adapter;
    }

    /**
     * When not constructed with an adapter, this method is called during
     * construction to create the defaule adapter.
     */
    protected void setupAdapter() {
        this.infiniteProgressAdapter = new CancelableProgessAdapter(this);
    }

    /**
     * Add a cancel listener to be called back when the user cancels the
     * progress.
     *
     * @param listener
     *            some listener that wants to take action on cancel (like stop
     *            whatever was being waited for)
     */
    @Override
    public void addCancelListener(final ActionListener listener) {
        ((CancelableProgessAdapter) this.infiniteProgressAdapter)
                .addCancelListener(listener);
    }

    /**
     * Remove a cancel listener that would be called back when the user cancels
     * the progress.
     *
     * @param listener
     *            some listener that wants to take action on cancel (like stop
     *            whatever was being waited for)
     */
    @Override
    public void removeCancelListener(final ActionListener listener) {
        ((CancelableProgessAdapter) this.infiniteProgressAdapter)
                .removeCancelListener(listener);
    }

    /**
     * Programmaticlly click the cancel button. Can be called from any thread.
     */
    public void doCancel() {
        ((CancelableProgessAdapter) this.infiniteProgressAdapter).doCancel();
    }
}
