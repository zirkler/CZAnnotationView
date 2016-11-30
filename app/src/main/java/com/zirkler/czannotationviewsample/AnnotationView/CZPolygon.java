package com.zirkler.czannotationviewsample.AnnotationView;

import java.util.ArrayList;
import java.util.List;

/**
 * The 2D polygon. <br>
 * 
 * @see {@link Builder}
 */
public class CZPolygon
{
	private final BoundingBox _boundingBox;
	private final List<CZLine> _sides;

	private CZPolygon(List<CZLine> sides, BoundingBox boundingBox)
	{
		_sides = sides;
		_boundingBox = boundingBox;
	}

	/**
	 * Get the builder of the polygon
	 * 
	 * @return The builder
	 */
	public static Builder Builder()
	{
		return new Builder();
	}

	/**
	 * Check if the the given point is inside of the polygon.<br>
	 *
	 * @param point
	 *            The point to check
	 * @return <code>True</code> if the point is inside the polygon, otherwise return <code>False</code>
	 */
	public boolean contains(CZPoint point)
	{
		if (inBoundingBox(point))
		{
			CZLine ray = createRay(point);
			int intersection = 0;
			for (CZLine side : _sides)
			{
				if (intersect(ray, side))
				{
					// System.out.println("intersection++");
					intersection++;
				}
			}

			/*
			 * If the number of intersections is odd, then the point is inside the polygon
			 */
			if (intersection % 2 == 1)
			{
				return true;
			}
		}
		return false;
	}

	public List<CZLine> getSides()
	{
		return _sides;
	}

	/**
	 * By given ray and one side of the polygon, check if both lines intersect.
	 *
	 * @param ray
	 * @param side
	 * @return <code>True</code> if both lines intersect, otherwise return <code>False</code>
	 */
	private boolean intersect(CZLine ray, CZLine side)
	{
		CZPoint intersectPoint = null;

		// if both vectors aren't from the kind of x=1 lines then go into
		if (!ray.isVertical() && !side.isVertical())
		{
			// check if both vectors are parallel. If they are parallel then no intersection point will exist
			if (ray.getA() - side.getA() == 0)
			{
				return false;
			}

			float x = ((side.getB() - ray.getB()) / (ray.getA() - side.getA())); // x = (b2-b1)/(a1-a2)
			float y = side.getA() * x + side.getB(); // y = a2*x+b2
			intersectPoint = new CZPoint(x, y);
		}

		else if (ray.isVertical() && !side.isVertical())
		{
			float x = ray.getStart().x;
			float y = side.getA() * x + side.getB();
			intersectPoint = new CZPoint(x, y);
		}

		else if (!ray.isVertical() && side.isVertical())
		{
			float x = side.getStart().x;
			float y = ray.getA() * x + ray.getB();
			intersectPoint = new CZPoint(x, y);
		}

		else
		{
			return false;
		}

		// System.out.println("Ray: " + ray.toString() + " ,Side: " + side);
		// System.out.println("Intersect point: " + intersectPoint.toString());

		if (side.isInside(intersectPoint) && ray.isInside(intersectPoint))
		{
			return true;
		}

		return false;
	}

	/**
	 * Create a ray. The ray will be created by given point and on point outside of the polygon.<br>
	 * The outside point is calculated automatically.
	 *
	 * @param point
	 * @return
	 */
	private CZLine createRay(CZPoint point)
	{
		// create outside point
		float epsilon = (_boundingBox.xMax - _boundingBox.xMin) / 100f;
		CZPoint outsidePoint = new CZPoint(_boundingBox.xMin - epsilon, _boundingBox.yMin);

		CZLine vector = new CZLine(outsidePoint, point);
		return vector;
	}

	/**
	 * Check if the given point is in bounding box
	 *
	 * @param point
	 * @return <code>True</code> if the point in bounding box, otherwise return <code>False</code>
	 */
	private boolean inBoundingBox(CZPoint point)
	{
		if (point.x < _boundingBox.xMin || point.x > _boundingBox.xMax || point.y < _boundingBox.yMin || point.y > _boundingBox.yMax)
		{
			return false;
		}
		return true;
	}

	/**
	 * Builder of the polygon
	 *
	 * @author Roman Kushnarenko (sromku@gmail.com)
	 */
	public static class Builder
	{
		private List<CZPoint> _vertexes = new ArrayList<CZPoint>();
		private List<CZLine> _sides = new ArrayList<CZLine>();
		private BoundingBox _boundingBox = null;

		private boolean _firstPoint = true;
		private boolean _isClosed = false;

		/**
		 * Add vertex points of the polygon.<br>
		 * It is very important to add the vertexes by order, like you were drawing them one by one.
		 *
		 * @param point
		 *            The vertex point
		 * @return The builder
		 */
		public Builder addVertex(CZPoint point)
		{
			if (_isClosed)
			{
				// each hole we start with the new array of vertex points
				_vertexes = new ArrayList<CZPoint>();
				_isClosed = false;
			}

			updateBoundingBox(point);
			_vertexes.add(point);

			// add line (edge) to the polygon
			if (_vertexes.size() > 1)
			{
				CZLine Line = new CZLine(_vertexes.get(_vertexes.size() - 2), point);
				_sides.add(Line);
			}

			return this;
		}

		/**
		 * Close the polygon shape. This will create a new side (edge) from the <b>last</b> vertex point to the <b>first</b> vertex point.
		 *
		 * @return The builder
		 */
		public Builder close()
		{
			validate();

			// add last CZLine
			_sides.add(new CZLine(_vertexes.get(_vertexes.size() - 1), _vertexes.get(0)));
			_isClosed = true;

			return this;
		}

		/**
		 * Build the instance of the polygon shape.
		 *
		 * @return The polygon
		 */
		public CZPolygon build()
		{
			validate();

			// in case you forgot to close
			if (!_isClosed)
			{
				// add last CZLine
				_sides.add(new CZLine(_vertexes.get(_vertexes.size() - 1), _vertexes.get(0)));
			}

			CZPolygon polygon = new CZPolygon(_sides, _boundingBox);
			return polygon;
		}

		/**
		 * Update bounding box with a new point.<br>
		 *
		 * @param point
		 *            New point
		 */
		private void updateBoundingBox(CZPoint point)
		{
			if (_firstPoint)
			{
				_boundingBox = new BoundingBox();
				_boundingBox.xMax = point.x;
				_boundingBox.xMin = point.x;
				_boundingBox.yMax = point.y;
				_boundingBox.yMin = point.y;

				_firstPoint = false;
			}
			else
			{
				// set bounding box
				if (point.x > _boundingBox.xMax)
				{
					_boundingBox.xMax = point.x;
				}
				else if (point.x < _boundingBox.xMin)
				{
					_boundingBox.xMin = point.x;
				}
				if (point.y > _boundingBox.yMax)
				{
					_boundingBox.yMax = point.y;
				}
				else if (point.y < _boundingBox.yMin)
				{
					_boundingBox.yMin = point.y;
				}
			}
		}

		private void validate()
		{
			if (_vertexes.size() < 3)
			{
				throw new RuntimeException("CZPolygon must have at least 3 points");
			}
		}
	}

	private static class BoundingBox
	{
		public float xMax = Float.NEGATIVE_INFINITY;
		public float xMin = Float.NEGATIVE_INFINITY;
		public float yMax = Float.NEGATIVE_INFINITY;
		public float yMin = Float.NEGATIVE_INFINITY;
	}
}