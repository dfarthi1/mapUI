package org.cci.mapui;

@SuppressWarnings("unused")
public class Node {
	
	private final String id; //room number
	private final int x,y;
	private final String adjList;
	
	//no arg constructor
	public Node(){
		id = null;
		x = -1;
		y = -1;
		adjList = null;
	}

	//constructor for a room that has no associated name
        public Node(String id, int x, int y, String adjList){
                this.id = id;
                this.x = x;
                this.y = y;
                this.adjList = adjList;
        }
	
	public String getId(){
		return id;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public String getAdjList(){
		return adjList;
	}
}
