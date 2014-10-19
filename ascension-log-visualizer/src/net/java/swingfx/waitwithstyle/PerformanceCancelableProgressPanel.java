package net.java.swingfx.waitwithstyle;

import java.awt.event.ActionListener;

/**
 * A progress panel with good performance that is cancelable instead of
 * infinite. The work is done by the CancelableAdaptee.
 * <p>
 * Allows programatic cancelling (maybe useful for timeouts), and adding a
 * listener to the user's cancel so that the action the user is waiting on is
 * stopped or ignored.
 *
 * @author Michael Bushe michael@bushe.com
 */
public class PerformanceCancelableProgressPanel extends
        PerformanceInfiniteProgressPanel {
    /**
     * 
     */
    private static final long serialVersionUID = -2286874342721309787L;

    public PerformanceCancelableProgressPanel() {
        super(true);
        this.setInfiniteProgressAdapter(this.createCancellableAdapter());
    }

    public PerformanceCancelableProgressPanel(final boolean i_bUseBackBuffer) {
        super(i_bUseBackBuffer);
        this.setInfiniteProgressAdapter(this.createCancellableAdapter());
    }

    public PerformanceCancelableProgressPanel(final int numBars) {
        super(numBars);
        this.setInfiniteProgressAdapter(this.createCancellableAdapter());
    }

    public PerformanceCancelableProgressPanel(
            final InfiniteProgressAdapter infiniteProgressAdapter) {
        super(infiniteProgressAdapter);
        this.setInfiniteProgressAdapter(this.createCancellableAdapter());
    }

    public PerformanceCancelableProgressPanel(final boolean i_bUseBackBuffer,
            final int numBars) {
        super(i_bUseBackBuffer, numBars);
        this.setInfiniteProgressAdapter(this.createCancellableAdapter());
    }

    public PerformanceCancelableProgressPanel(final boolean i_bUseBackBuffer,
            final CancelableProgessAdapter infiniteProgressAdapter) {
        super(i_bUseBackBuffer, infiniteProgressAdapter);
    }

    public PerformanceCancelableProgressPanel(final int numBars,
            final CancelableProgessAdapter infiniteProgressAdapter) {
        super(numBars, infiniteProgressAdapter);
    }

    public PerformanceCancelableProgressPanel(final boolean i_bUseBackBuffer,
            final int numBars,
            final CancelableProgessAdapter infiniteProgressAdapter) {
        super(i_bUseBackBuffer, numBars, infiniteProgressAdapter);
    }

    /**
     * When not constructed with a CancelableProgressAdapter this method is
     * called on construction to create one.
     *
     * @return a cancellable adapter
     */
    protected CancelableProgessAdapter createCancellableAdapter() {
        return new CancelableProgessAdapter(this);
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
