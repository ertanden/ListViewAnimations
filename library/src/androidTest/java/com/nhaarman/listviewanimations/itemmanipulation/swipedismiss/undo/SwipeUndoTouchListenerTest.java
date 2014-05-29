package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.AbsListView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeTouchListenerTestActivity;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.MotionEventUtils.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SwipeUndoTouchListenerTest extends ActivityInstrumentationTestCase2<SwipeTouchListenerTestActivity> {

    /**
     * An Activity hosting a ListView with items.
     */
    private SwipeTouchListenerTestActivity mActivity;

    /**
     * The AbsListView that is hosted in mActivity.
     */
    private AbsListView mAbsListView;

    @Mock
    private UndoCallback mUndoCallback;


    public SwipeUndoTouchListenerTest() {
        super(SwipeTouchListenerTestActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        MockitoAnnotations.initMocks(this);
        when(mUndoCallback.getUndoView(any(View.class))).thenReturn(new View(getActivity()));
        when(mUndoCallback.getPrimaryView(any(View.class))).thenReturn(new View(getActivity()));

        mActivity = getActivity();
        mAbsListView = mActivity.getAbsListView();

        View.OnTouchListener swipeUndoTouchListener = new SwipeUndoTouchListener(mAbsListView, mUndoCallback);
        mAbsListView.setOnTouchListener(swipeUndoTouchListener);

        getInstrumentation().waitForIdleSync();
    }

    /**
     * Tests whether swiping an item once triggers UndoCallback#onUndoShown.
     */
    public void testUndoShown() throws InterruptedException {
        dispatchSwipeMotionEventsAndWait(mActivity, mAbsListView, 0);

        verify(mUndoCallback).onUndoShown(any(View.class), eq(0));
    }

    /**
     * Tests whether swiping an item twice triggers UndoCallback#onDismiss.
     */
    public void testDismiss() throws InterruptedException {
        dispatchSwipeMotionEventsAndWait(mActivity, mAbsListView, 0);

        verify(mUndoCallback).onUndoShown(any(View.class), eq(0));
        verify(mUndoCallback, never()).onDismiss(any(View.class), anyInt());

        dispatchSwipeMotionEventsAndWait(mActivity, mAbsListView, 0);

        verify(mUndoCallback).onDismiss(any(View.class), eq(0));
    }

    /**
     * Tests whether swiping multiple items triggers onUndoShown, but not onDismiss.
     */
    public void testMultipleUndo() throws InterruptedException {
        dispatchSwipeMotionEventsAndWait(mActivity, mAbsListView, 0);

        verify(mUndoCallback).onUndoShown(any(View.class), eq(0));

        dispatchSwipeMotionEventsAndWait(mActivity, mAbsListView, 1);

        verify(mUndoCallback).onUndoShown(any(View.class), eq(1));

        dispatchSwipeMotionEventsAndWait(mActivity, mAbsListView, 2);

        verify(mUndoCallback).onUndoShown(any(View.class), eq(2));

        verify(mUndoCallback, never()).onDismiss(any(View.class), anyInt());
    }

    /**
     * Tests whether multiple dismisses are correctly handled.
     */
    public void testMultipleDismisses() throws InterruptedException {
        dispatchSwipeMotionEvents(mActivity, mAbsListView, 0);
        dispatchSwipeMotionEvents(mActivity, mAbsListView, 1);
        dispatchSwipeMotionEventsAndWait(mActivity, mAbsListView, 2);

        verify(mUndoCallback, times(3)).onUndoShown(any(View.class), anyInt());
        verify(mUndoCallback, never()).onDismiss(any(View.class), anyInt());

        dispatchSwipeMotionEvents(mActivity, mAbsListView, 0);
        dispatchSwipeMotionEvents(mActivity, mAbsListView, 1);
        dispatchSwipeMotionEventsAndWait(mActivity, mAbsListView, 2);

        verify(mUndoCallback, times(3)).onDismiss(any(View.class), anyInt());
        verify(mUndoCallback).onDismiss(eq(mAbsListView), aryEq(new int[]{2, 1, 0}));
    }
}