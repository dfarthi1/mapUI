/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cci.mapui;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.*;

import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import java.io.*;

public class WoodwardGraph {

    //set up Graph object for Woodward Hall map
    //create an array list of vertices as nodes
    private static final ArrayList<Node> vertexList = new ArrayList<>();
    
    //create an array list of vertices as string
    private final ArrayList<String> v = new ArrayList<>();
    
    //create an Undirected Graph of Nodes
    private final SimpleWeightedGraph<Node, DefaultWeightedEdge> g =
        new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

    /*
    *   method searches through vertexList and returns the node of the vertex with the given String id
    */
    public static Node getVertex(String id)
    {
            for(Node n:vertexList)
            {
                    if(n.getId().equals(id))
                    {
                            return n;
                    }
            }
            return null;
    }
    /*
    *   method returns the vertexList
    */
    public ArrayList<String> getVertexList()
    {
        return v;
    }

    public WoodwardGraph() throws IOException
    {	
        //dummy variable to read in lines of data from files
        String line;

        //Scanner for reading the list of names associated with room numbers
        Scanner inputName = new Scanner(new File("C:\\Users\\Dave\\Documents\\NetBeansProjects\\MapUI\\src\\org\\cci\\mapui\\NameList.txt"));

        //create an array list of names and fill it with the data from the name list file
        ArrayList<Name> nameList = new ArrayList<>();
        String[] nameListData;

        while(inputName.hasNext())
        {
                line = inputName.nextLine();
                nameListData = line.split(",");
                nameList.add(new Name(nameListData[1],nameListData[0],nameListData[2])); //last name comes before first name in data file
        }

        inputName.close();

        Scanner input = new Scanner(new File("C:\\Users\\Dave\\Documents\\NetBeansProjects\\MapUI\\src\\org\\cci\\mapui\\AdjacencyList.txt"));

        String[] vertexData = {""};
        String[] nodeList = {""};
        int x,y;

        //read in data for graph from file
        while(input.hasNext())
        {
                line = input.nextLine();
                v.add(line);
        } 
        input.close();

        //create Node objects out of each line of vertex data and add to graph
        Node thisNode = null;
        for(String s:v)
        {
                vertexData = s.split(",");
                //get Location x,y
                x = Integer.parseInt(vertexData[1]);
                y = Integer.parseInt(vertexData[2]);
                thisNode = new Node(vertexData[0],x,y,vertexData[3]);
                vertexList.add(thisNode);
                g.addVertex(thisNode);
        }

        //get a set of all vertices of graph
        Set<Node> vertexSet = g.vertexSet();

                //iterate through the set until the vertex is found
        Iterator<Node> itr = vertexSet.iterator();

        //create a temporary node to hold all the end nodes of edges to be added to graph
        //create a node n to hold the start node for all the edges to be added to graph
        Node tempNode = null;
        Node n = null;

        //iterate over the set to get all the possible edges from each vertex to the vertices in its adjacency list
        while(itr.hasNext())
        {
                n = itr.next();
                //create a list of all the node id's (room numbers) that the current node leads to 
                nodeList = n.getAdjList().split("/");

                //iterate over each of the nodes in the nodeList
                //start node of the edge to be added is n
                //end node of the edge to be added needs to be found in the graph
                for(String currNode:nodeList)
                {
                        tempNode = getVertex(currNode);
                        //if currNode was found in vertexList then add the edge from node n to the tempNode
                        if(tempNode!=null)
                        {	
                                        DefaultWeightedEdge e = new DefaultWeightedEdge();
                                e = g.addEdge(n, tempNode);
                        }
                }

        } //end  while(itr.hasNext())

        //loop through set of edges and add weights to all edges containing stairs
        //this forces the get shortest path algorithm not to include stairs unless the path must change floors
        Set<DefaultWeightedEdge> edgeSet = g.edgeSet();
        Iterator<DefaultWeightedEdge> edgeItr = edgeSet.iterator();
        DefaultWeightedEdge currEdge = new DefaultWeightedEdge();
        Node edgeSource = null, edgeTarget = null;

        while(edgeItr.hasNext())
        {
                currEdge = edgeItr.next();
                edgeSource = g.getEdgeSource(currEdge);
                edgeTarget = g.getEdgeTarget(currEdge);

                if(edgeSource.getId().startsWith("Stair") || edgeTarget.getId().startsWith("Stair"))
                {
                        g.setEdgeWeight(currEdge, 5.0);
                }
        }
        }//end constructor for WoodwardHallGraph

    /*	
     * getShortestPath method returns shortest path from entrance to given room number
     */

    public List<DefaultWeightedEdge> getShortestPath(String endOfPath)
    {
        Node startNode=null, endNode=null;	//node objects to represent the start and end of the path

        //search array list of vertices to find index of end of path
        boolean found = false;
        while(!found)
        {
                for(String vertex:v)
                {

                        if(vertex.startsWith(endOfPath))
                        {
                                found = true;
                                endNode = getVertex(endOfPath);
                        }
                }
        }

        startNode = getVertex("Entrance");
        //create a DijkstraShortestPath object
        DijkstraShortestPath<Node,DefaultWeightedEdge> shortestPath = new DijkstraShortestPath<>(g,startNode,endNode);

        //create a list of edges in shortest path
        List<DefaultWeightedEdge> listOfEdges = shortestPath.getPathEdgeList();

        // code to test edge list by printing it out
        /*
        for(DefaultWeightedEdge e: listOfEdges)
        {
                Node source = g.getEdgeSource(e);
                Node target = g.getEdgeTarget(e);
                System.out.println(source.getId() + " : " + target.getId());
        }*/

        return listOfEdges;
    }//end getShortestPath method
    
    public List<DefaultWeightedEdge> getShortestPath(String startOfPath, String endOfPath)
    {
        Node startNode=null, endNode=null;	//node objects to represent the start and end of the path

        //search array list of vertices to find index of start of path
        boolean found = false;
        while(!found)
        {
                for(String vertex:v)
                {

                        if(vertex.startsWith(startOfPath))
                        {
                                found = true;
                                startNode = getVertex(startOfPath);
                        }
                }
        }
        //search array list of vertices to find index of end of path
        found = false;
        while(!found)
        {
                for(String vertex:v)
                {

                        if(vertex.startsWith(endOfPath))
                        {
                                found = true;
                                endNode = getVertex(endOfPath);
                        }
                }
        }
        //create a DijkstraShortestPath object
        DijkstraShortestPath<Node,DefaultWeightedEdge> shortestPath = new DijkstraShortestPath<>(g,startNode,endNode);

        //create a list of edges in shortest path
        List<DefaultWeightedEdge> listOfEdges = shortestPath.getPathEdgeList();

        // code to test edge list by printing it out
        /*
        for(DefaultWeightedEdge e: listOfEdges)
        {
                Node source = g.getEdgeSource(e);
                Node target = g.getEdgeTarget(e);
                System.out.println(source.getId() + " : " + target.getId());
        }*/

        return listOfEdges;
    }//end getShortestPath method

    /*	
     * getNodeList method returns an ArrayList of Nodes in the given shortest path
     */
    public ArrayList<Node> getNodeList(List<DefaultWeightedEdge> nodeList)
    {
        ArrayList<Node> nodes = new ArrayList<>();
        Node start = null;
        Node end = null;
        
        for(DefaultWeightedEdge e:nodeList)
        {
            start = g.getEdgeSource(e);
            end = g.getEdgeTarget(e);
            
            if(!nodes.contains(start)) nodes.add(start);
            if(!nodes.contains(end)) nodes.add(end);
        }
        
        return nodes;
    }
}// end WoodwardGraph class

