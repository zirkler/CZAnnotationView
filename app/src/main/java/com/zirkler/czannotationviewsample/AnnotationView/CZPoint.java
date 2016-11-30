package com.zirkler.czannotationviewsample.AnnotationView;

/**
 * CZPoint on 2D landscape
 *
 */
public class CZPoint
{
	public float x;
	public float y;
	public CZPoint(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString()
	{
		return String.format("(%.2f,%.2f)", x, y);
	}
}