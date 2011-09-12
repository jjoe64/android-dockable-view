package de.inovex.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Dockable View
 * This view can be expanded/collapsed with a move gesture.
 * Override {@link #getExpandedHeight()} and {@link #getDockedSize()} to customize the height.
 */
public abstract class DockableView extends LinearLayout {
	private class SlideAnimation extends Animation {
		private final int sourceHeight;
		private final int targetHeight;

		public SlideAnimation(boolean slideDown) {
			this.sourceHeight = getHeight();
			this.targetHeight = slideDown?getDockedSize():getExpandedHeight();
			setDuration(1000);
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			int newHeight = sourceHeight + (int)((targetHeight - sourceHeight)*interpolatedTime);
			setHeight(newHeight);
		}

		@Override
		public boolean willChangeBounds() {
			return true;
		}
	}

	private GestureDetector gestureDetector;
	private float lastEventY;

	public DockableView(Context context, AttributeSet attrs) {
		super(context, attrs);

		gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				if (velocityY < 0) {
					slide(false);
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * override this to set the height for this view when it is docked.
	 * @return
	 */
	protected int getDockedSize() {
		return 55;
	}

	/**
	 * this implementation returns the parent height.
	 * you can override this, if you don't want that the view get fullsized when expanded.
	 * @return expanded height
	 */
	protected int getExpandedHeight() {
		View parent = (View) getParent();
		return parent.getHeight();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (getAnimation() != null && getAnimation().hasStarted()) {
			return false;
		}
		if ((event.getAction() & MotionEvent.ACTION_MOVE) > 0) {
			if (lastEventY > 0) {
				float diff = lastEventY-event.getY();
				setHeight(getHeight()+diff);
				lastEventY = event.getY() + diff;
			} else lastEventY = event.getY();
		}
		boolean r = gestureDetector.onTouchEvent(event);
		if ((event.getAction() & MotionEvent.ACTION_UP) > 0) {
			lastEventY = 0f;
			if (!r) {
				slide(true);
			}
		}
		return r;
	}

	private void setHeight(float height) {
		RelativeLayout.LayoutParams layoutP = (android.widget.RelativeLayout.LayoutParams) getLayoutParams();
		layoutP.height = (int) height;
		setLayoutParams(layoutP);
	}

	private void slide(boolean down) {
		Animation anim = new SlideAnimation(down);
		setAnimation(anim);
	}
}
