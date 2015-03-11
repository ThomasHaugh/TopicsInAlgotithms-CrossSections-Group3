package b_object3D_collision;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static java.lang.Math.*;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class SegmentedPaths {
    private ArrayList<SegmentedPath> paths = new ArrayList<SegmentedPath>();

    void addPath( SegmentedPath path ) { paths.add(path); }

    void removeLast() { paths.remove(paths.size()-1); }
    
    int size() { return paths.size(); }
    
    void addRemovableSegmentChecked( Vctr3D pA, Vctr3D pB ) { // Adds a new, but "to be removed, unless cut" 
        SegmentedPath newSegment = new SegmentedPath( pA );   //   segment if it is long enough
        if ( newSegment.addNextRemovablePointChecked( pB ) ) { addPath(newSegment); }
    }

    /*
      This method takes bubbles and puts them in bigger bubbles then adds them to sets
    */
    ArrayList<Integer>[] getOverlapsOfBubblesAndPutThemInSets (SegmentedPaths paths) {
	ArrayList<Integer>[] unionFinds = new ArrayList<Integer>[paths.size()];
	for(int i = 0; i<paths.size();i++) {
	    unionFinds[i].add(i); 
	}
	for(int i = 0; i<paths.size();i++) {
	    for (int j = i+1; j<paths.size();j++) {
		SegmentedPath pathA = paths.get(i);
		SegmentedPath pathB = paths.get(j);
		if (pathA.overlap(pathB) != null) {
		    unionFinds[j].addAll(unionFinds[i]);
		    for(int iPath : unionFinds[j]) {
			unionFinds[iPath] = unionFinds[j]; 
		    }
		}
	    }
	}
	return unionFinds;
    }

    SegmentedPaths getSetBubble (SegmentedPaths paths, ArrayList<Integer>[] unionFind) {
	SegmentedPaths offsetPaths = new SegmentedPaths(); 
	for(int i = 0; i<paths.size();i++) {
	    for(ArrayList<integer> pathnums : unionFinds[i]) {
		path = getOuterBubble(pathNum);
		offsetPaths.addPath(path);
	    }
	}
	return offSetPaths
    }

    SegmentedPath getOuterBubble
	
    // Calculate the offsets
    SegmentedPaths offsetStage1(Group path2D, float offset) {
        SegmentedPaths offsetPaths = new SegmentedPaths();

        Vctr3D offsetVector = new Vctr3D(0,0,0);

        for ( SegmentedPath path : paths ) {
            Vctr3D pointA = path.get(path.size()-1); // Get the last point in path
            Vctr3D pointB = path.getStart();
            Vctr3D pointOffsetA1 = null;
            Vctr3D pointOffsetB1 = null;
            Vctr3D pointOffsetB2 = new Vctr3D(pointB);
            Vctr3D pointOffsetC2 = null;

            Vctr3D pointStrtPath = null;
            if ( pointA.distL1(pointB) < .5 ) { // We assume that it is the same point
                pointA = path.get(path.size()-2);
                pointOffsetA1 = new Vctr3D(pointA);
                pointOffsetB1 = new Vctr3D(pointB);
                pointStrtPath = pointOffsetB1; // This will close the path to this point
                offsetVector.x = pointB.y - pointA.y;
                offsetVector.y = pointA.x - pointB.x;
                float offsetDist = (float) Math.sqrt(offsetVector.x*offsetVector.x+offsetVector.y*offsetVector.y);
                if (offsetDist>0) {
                    offsetVector.x *= offset/offsetDist;
                    offsetVector.y *= offset/offsetDist;

                    pointOffsetA1.x += offsetVector.x;
                    pointOffsetA1.y += offsetVector.y;
                    pointOffsetB1.x += offsetVector.x;
                    pointOffsetB1.y += offsetVector.y;
                }
            } else { // We assume that the path is not a cycle
                pointA = null;
            }

            SegmentedPath offsetPath = new SegmentedPath(); 
            offsetPaths.addPath( offsetPath );  
            for (int iSegment = 0; iSegment < path.size(); iSegment++ ) {
                Vctr3D pointC = path.get(iSegment);

                offsetVector.x = pointC.y - pointB.y;
                offsetVector.y = pointB.x - pointC.x;
                float offsetDist = (float) Math.sqrt(offsetVector.x*offsetVector.x+offsetVector.y*offsetVector.y);
                if (offsetDist>0) {
                    offsetVector.x *= offset/offsetDist;
                    offsetVector.y *= offset/offsetDist;
    
                    pointOffsetC2 = new Vctr3D(pointC);
                    pointOffsetB2.x += offsetVector.x;
                    pointOffsetB2.y += offsetVector.y;
                    pointOffsetC2.x += offsetVector.x;
                    pointOffsetC2.y += offsetVector.y;
                    
                    if (pointA == null) {
                        // We skip doing anything. There is a lag with added offsets.
                    } else { // pointA != null
                        float v1x = pointB.x-pointA.x;
                        float v1y = pointB.y-pointA.y;
                        float v2x = pointC.x-pointB.x;
                        float v2y = pointC.y-pointB.y;
                        float dotProduct = v1x * v2x + v1y * v2y ;
                        float crsProduct = v1x * v2y - v1y * v2x ;
                        float crossTol = ((float)(1e-4))*dotProduct; // Tolerance for cross-product
                        if ( -crossTol < crsProduct && crsProduct < crossTol ) { // parallel ill-conditioned
                            crsProduct = crossTol;
                        }
                        if ( dotProduct >= 0 && crsProduct > 0 ) { // Angle of turn > 0 degrees and < 90 degrees
                            // We extend pointOffsetB1 and pointOffsetB2 so that they meet at the same spot
                            float [] extendedPoint2D = pointOffsetB1.getExtrapolateIntersect2D(
                                    pointOffsetB2, v1x, v1y, v2x, v2y, crsProduct);

                            pointOffsetB1.x = pointOffsetB2.x = extendedPoint2D[0];
                            pointOffsetB1.y = pointOffsetB2.y = extendedPoint2D[1];
                            offsetPath.addNextPoint(pointOffsetB1); 
                        } else if ( crsProduct > 0 ) { // Angle of turn > 0 degrees and > 90 degrees (sharp convex turn)
                            // We extend pointOffsetB1 and pointOffsetB2 and add an extra segment in the middle
                            float [] extendedPoint2D = pointOffsetB1.getExtrapolateTwoIntersect2D(
                                    pointOffsetB2, pointB, offset, v1x, v1y, v2x, v2y, crsProduct);

                            pointOffsetB1.x = extendedPoint2D[0];
                            pointOffsetB1.y = extendedPoint2D[1];
                            pointOffsetB2.x = extendedPoint2D[2];
                            pointOffsetB2.y = extendedPoint2D[3];
                            offsetPath.addNextPoint(pointOffsetB1); 
                            offsetPath.addNextPoint(pointOffsetB2); 
                        } else { // TODO . We are skipping a sharp in check and in or out turn check
                            // Finish the old path
                            offsetPath.addNextPoint(pointOffsetB1); 

                            if (iSegment == 0) pointStrtPath = null; // Do not close the path

                            // Start a new path
                            offsetPaths.addPath( offsetPath = new SegmentedPath(pointOffsetB2) ); 
                        }
                    }

                    pointA = pointB; // null; // 
                    pointB = pointC;
                    pointOffsetA1 = pointOffsetB2;
                    pointOffsetB1 = pointOffsetC2;
                    pointOffsetB2 = new Vctr3D(pointB);
                }
            }
            if (pointStrtPath != null) { // null should only occur for zero length path
                offsetPath.addNextPoint(pointStrtPath);
            }
        }
        
        return offsetPaths;
    }
    
    void offsetStage2() { // Find and remove intersections between offset segments
        
        // Break up paths into individual segments.
        //   We are doing this just for the class. 
        //   It would be hard for students to work with a double array structure.
        //   I had it working without this break up previously.
        SegmentedPaths newPaths = new SegmentedPaths();
        for ( int iPath = 0; iPath < paths.size(); iPath++ ) {
            SegmentedPath path  = paths.get(iPath);
            
            Vctr3D point = path.getStart();
            for (int iSegment = 0; iSegment < path.size(); iSegment++ ) {
                SegmentedPath newPath = new SegmentedPath( new Vctr3D(point) ); // This point is copied
                newPath.addNextPoint( point = path.get(iSegment) );             // This point is reused
                newPaths.addPath(newPath);
            }
        }
        paths = newPaths.paths;

        // INDIVIDUAL ASSIGNMENT TARGET ZONE START ****************************************************************************
        // Iterate through all the segments (that is the first two loops)
	/*
	for ( int iPathA = 0; iPathA < paths.size(); iPathA++ ) {
            SegmentedPath pathA  = paths.get(iPathA);
            Vctr3D pointA1 = pathA.getStart();
            Vctr3D pointA2 = pathA.get(0);

            // For each segment of the outer loop, iterate through all segments that appear later
            for ( int iPathB = iPathA+1; iPathB < paths.size(); iPathB++ ) {
                SegmentedPath pathB  = paths.get(iPathB);
                Vctr3D pointB1 = pathB.getStart();
                Vctr3D pointB2 = pathB.get(0);

                // Compare segment (pointA1, pointA2) with (pointB1,pointB2) for an intersection
                float [] intersect = pointA1.getIntersect2D(pointA2, pointB1, pointB2);
                if ( intersect != null ) {
                    // Remove intersections between offset segments
                    offsetStage2_processIntersection( intersect, iPathA, iPathB );
                    // NOTE, offsetStage2_processIntersection() WILL ADD SEGMENTS TO this.paths
                }
            }
        }
	*/
	
	Collections.sort(paths, new Comparator<SegmentedPath> () {
		@Override
		public int compare( SegmentedPath pathA, SegmentedPath pathB)
		{
		    Float xA1 = pathA.getStart().x;
		    Float xA2 = pathA.get(0).x;
		    Float xB1 = pathB.getStart().x;
		    Float xB2 = pathB.get(0).x;
		    Float xA  = min(xA1,xA2);
		    Float xB  = min(xB1,xB2);
		    return xA.compareTo(xB);
		}
	    });
	
	
	List<Integer> checkList = new ArrayList<Integer>();

	for ( int iPathA = 0; iPathA < paths.size(); iPathA++) {
	    SegmentedPath pathA = paths.get(iPathA);
	    Vctr3D A1 = pathA.getStart();
	    Vctr3D A2 = pathA.get(0);
	    Float stA = min(A1.x,A2.x);
	    if (!(checkList.isEmpty())){
		List<Integer> copy = new ArrayList<Integer>(checkList);
		for(Integer iPathB : checkList) {
		    SegmentedPath pathB = paths.get(iPathB.intValue());
		    Vctr3D B1 = pathB.getStart();
		    Vctr3D B2 = pathB.get(0);
		    Float edB = max(B1.x,B2.x);
		    if (edB < stA) {
			copy.remove(iPathB);
		    } else {
			float [] intersect = A1.getIntersect2D(A2,B1,B2);
			if (intersect != null) {
			    offsetStage2_processIntersection( intersect, iPathA, iPathB.intValue());
			}
		    }
		    
		}
		checkList = copy;
	    }
	    checkList.add(iPathA);
	}

        // INDIVIDUAL ASSIGNMENT TARGET ZONE END ******************************************************************************

        // Remove segments that need to be deleted
        for ( int iPath = 0; iPath < paths.size(); iPath++ ) {
            SegmentedPath path  = paths.get(iPath);
            
            if (path.isRemoveLast()) path.removeLast();
            
            if (path.size() < 1) {
                paths.remove(iPath);
                iPath--; // Note, bad form to modify iterator
            }
        }
    }
    
    void offsetStage2_processIntersection( // Remove intersections between offset segments
            float [] intersect, int iPathA, int iPathB ) { 

        SegmentedPath pathA  = paths.get(iPathA);
        Vctr3D pointA1 = pathA.getStart();
        Vctr3D pointA2 = pathA.get(0);
        SegmentedPath pathB  = paths.get(iPathB);
        Vctr3D pointB1 = pathB.getStart();
        Vctr3D pointB2 = pathB.get(0);

        // Split the paths and shorten the segments
        if ( intersect[2] < 0 ) { // Cross-product is negative
            Vctr3D intersectPoint = new Vctr3D(intersect[0], intersect[1], pointB1.z);
            addRemovableSegmentChecked( new Vctr3D(pointB1), new Vctr3D( intersectPoint ) );
            pointB1.x = intersect[0];
            pointB1.y = intersect[1];
            if (pointA1.distL1(intersectPoint) < 0.1) {
                pathA.setRemoveLast();
            } else {
                addRemovableSegmentChecked( intersectPoint, new Vctr3D(pointA2) );
                pointA2.x = intersect[0];
                pointA2.y = intersect[1];
            }
        } else {
            Vctr3D intersectPoint = new Vctr3D(intersect[0], intersect[1], pointA1.z);
            if (pointB1.distL1(intersectPoint) < 0.1) {
                pathB.setRemoveLast();
            } else {
                addRemovableSegmentChecked( new Vctr3D(intersectPoint), new Vctr3D(pointB2) );
                pointB2.x = intersect[0];
                pointB2.y = intersect[1];
            }
            addRemovableSegmentChecked( new Vctr3D(pointA1), intersectPoint );
            pointA1.x = intersect[0];
            pointA1.y = intersect[1];
        }
    }

    // Remove all segments from offsetPaths when within offset from this.paths
    void offsetStage3(SegmentedPaths offsetPaths, float offset) { 
        
        float distThreshold = (1-0.001f)*offset - 0.00001f;

        // Iterate through all the segments (that is the first two loops)
        for ( SegmentedPath pathA  : paths ) {
            Vctr3D pointA1 = pathA.getStart();
            for (int iSegmentA = 0; iSegmentA < pathA.size(); iSegmentA++ ) {
                Vctr3D pointA2 = pathA.get(iSegmentA);


                // For each segment of the outer loop (path), iterate through all segments of offset paths
                for ( int iPathB = 0; iPathB < offsetPaths.size(); iPathB++ ) {
                    SegmentedPath pathB  = offsetPaths.paths.get(iPathB);
                    Vctr3D pointB1 = pathB.getStart();
                    for (int iSegmentB = 0; iSegmentB < pathB.size(); iSegmentB++ ) {
                        Vctr3D pointB2 = pathB.get(iSegmentB);


                        // Find segment (pointA1, pointA2) and (pointB1,pointB2) distance
                        float distance = pointA1.getDistance2D(pointA2, pointB1, pointB2);
                        if ( distance < distThreshold ) { 
                            // Remove segment in the offset paths
                            SegmentedPath pathSecondPartB = pathB.splitPath(iSegmentB); 
                            pathB.removeLast();
                            offsetPaths.addPath(pathSecondPartB);
                        }


                        pointB1 = pointB2;
                    }
                }


                pointA1 = pointA2;
            }
        }
    }
    
    void displayPaths(Group path2D, Color color) {

        for ( SegmentedPath path : paths ) {
            Vctr3D point = path.getStart();
            Path pathDraw = new Path();
            // First move to starting point
            MoveTo moveTo = new MoveTo();
            moveTo.setX(point.x);
            moveTo.setY(point.y);
            pathDraw.getElements().add(moveTo);
            //System.out.println("MoveTo: "+point.x+", "+point.y);
            for (int iSegment = 0; iSegment < path.size(); iSegment++ ) {
                point = path.get(iSegment);
                // Then start drawing a line
                LineTo lineTo = new LineTo();
                lineTo.setX(point.x);
                lineTo.setY(point.y);
                pathDraw.getElements().add(lineTo);
                //System.out.println("LineTo: "+point.x+", "+point.y);
            }
            pathDraw.setStroke(color);
            path2D.getChildren().add(pathDraw);
        }
    }
    
    void combineSegmentPaths() {
        for ( int iPathA = 0; iPathA < paths.size(); iPathA++ ) {
            SegmentedPath pathA = paths.get(iPathA);
            Vctr3D pointLastA = pathA.get(pathA.size()-1);
            float minDistL1 = Float.MAX_VALUE;
            int   indxMinDst = -1;
            for ( int iPathB = 0; iPathB < paths.size(); iPathB++ ) { 
                if (  iPathA == iPathB ) continue; // cannot connect to itself
                SegmentedPath pathB = paths.get(iPathB);
                Vctr3D pointFirstB = pathB.getStart();
                
                float distL1 = pointLastA.distL1(pointFirstB);
                if (distL1 < minDistL1) {
                    indxMinDst = iPathB;
                    minDistL1 = distL1;
                }
            }
            
            if (minDistL1 < 1f) {
                // Add the paths into 1
                pathA.addPath(paths.get(indxMinDst));
                paths.remove(indxMinDst);
                pointLastA = pathA.get(pathA.size()-1);
                if ( indxMinDst < iPathA ) iPathA -= 2; // Note, this is bad form to modify the iterator
                else                       iPathA -= 1; // We want to repeat search with new A
            }
            else {
                //System.out.println("pointLastA match not found " + iPathA + " " + indxMinDst + " " + minDistL1 + " ");
            }
        }
    }
    
    void removeMinisculeSegments() {
        for ( int iPath = 0; iPath < paths.size(); iPath++ ) {
            SegmentedPath path = paths.get( iPath );
            path.removeMinisculeSegments();
            if ( path.size() < 1 ) { // If less than one segment, remove path
                paths.remove( iPath );
                iPath--;// Bad form to modify iterator
            }
        }
    }
    
    void temp() { // Special function to play with for debugging
        //paths.get(0).splitPath(paths.get(0).size()/2-1);
        //paths.get(1).splitPath(paths.get(1).size()/2-1);
        //removeLast();
    }
}
