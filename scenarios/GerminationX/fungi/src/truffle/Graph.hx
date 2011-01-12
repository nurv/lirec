// t r u f f l e Copyright (C) 2010 FoAM vzw   \_\ __     /\
//                                          /\    /_/    / /  
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package truffle;

class Edge
{
    public var From:Int;
    public var To:Int;
    public var Weight:Float;

    public function new(f:Int, t:Int, w:Float)
    {
        From=f;
        To=t;
        Weight=w;
    }

    public function Eq(other:Edge)
    {
        return ((From==other.From && To==other.To) ||
                (From==other.To && To==other.From));
    }
}

class Graph
{
    // edges consists of a list of lists:
    // [[node1,node2,weight],[node1,node2,weight]...]
    // the graph also internally keeps track of 
    // the unique vertices
    public var Edges:List<Edge>;
    public var Vertices:List<Int>;
    public var Root:Int;

    public function new(edges:List<Edge>)
    {
        Edges = edges;
        UpdateVertices();
        Root=999;
    }

    public function Print()
    {
        for (e in Edges)
        {
            trace(Std.string(e.From)+" <- "+Std.string(e.Weight)+" -> "+Std.string(e.To));
        }
        trace("root is "+Std.string(Root));
    }

    public function AddVertex(id:Int)
    {
        for (v in Vertices)
        {
            if (v==id) return;
        }
        Vertices.add(id);
    }
    
    // a safe way to add edges - checks if it exists already
    public function AddEdge(edge:Edge)
    {
        //if (!ContainsEdge(edge))
        {
            if (Root==999) Root=edge.From;
            Edges.add(edge);
            AddVertex(edge.From);
            AddVertex(edge.To);
        }
    }

    // remove the edge from the graph - warning, doesn't seem to update
    // the vertices set
    public function RemoveEdge(edge:Edge)
    {
        if (!Edges.remove(edge)) 
        {
            Edges.remove(new Edge(edge.From,edge.To,edge.Weight));
        }
    }

    // returns True if the graph contains this vertex
    public function ContainsVertex(vertex:Int) : Bool
    {
        for (v in Vertices)
        {
            if (v==vertex) return true;
        }
        return false;
    }

    public function UpdateVertices()
    {
        Vertices = new List<Int>();
        for (edge in Edges)
        {
            AddVertex(edge.From);
            AddVertex(edge.To);
        }
    }
    
    // returns the number of vertices in the graph
    public function NumVertices()
    {
        return Vertices.length;
    }

    // returns true if the graph contains this edge
    public function ContainsEdge(edge:Edge)
    {
        for (e in Edges)
        {
            if (e.Eq(edge))  return true;
        }
        return false;
    }

    // returns edges connected to the vertex
    public function GetEdges(vertex:Int)
    {
        var ret = new List<Edge>();
        for (edge in Edges)
        {
            if (edge.From==vertex) ret.add(edge);
            //if (edge.To==vertex) ret.add(edge);
        }
        return ret;
    }

    // returns the cheapest edge in the whole graph
    public function GetCheapestEdge()
    {
        var lowest_cost = 9999.0;
        var cheapest_edge = new Edge(-1,-1,9999.0);
        for (edge in Edges)
        {
            if (edge.Weight<lowest_cost)
            {
                lowest_cost=edge.Weight;
                cheapest_edge=edge;
            }
        }
        return cheapest_edge;
    }

    // returns the minimum spanning tree of this graph
    // using prim's algorithm. this will fail and return False if 
    // the graph is not a single connected component, or if 
    // given a starting vertex not in the graph
    public function MST(vertex:Int) : Graph
    {
        var candidate_edges = new Graph(new List<Edge>());
        var mst = new Graph(new List<Edge>());
        mst.AddVertex(vertex);
        while (mst.NumVertices()<NumVertices())
        {
            // get the edges from the current vertex
            for (edge in GetEdges(vertex))
            {
                // one of the vertices in the edge must be new
                if (!(mst.ContainsVertex(edge.From) && 
                      mst.ContainsVertex(edge.To)))
                {
                    // add them to the candidates for lookup
                    candidate_edges.AddEdge(edge);
                }
            }
            // find and remove the cheapest edge from the candidates
            var cheapest = candidate_edges.GetCheapestEdge();
            if (cheapest.From==-1 || cheapest.To==-1) 
            {
                trace("MST error");
                return new Graph(new List<Edge>());
            }
            candidate_edges.RemoveEdge(cheapest);

            // see if this edge is needed
            if (! (mst.ContainsVertex(cheapest.From) && 
                   mst.ContainsVertex(cheapest.To)))
             {
                 // find out which vertex to move to
                 if (mst.ContainsVertex(cheapest.From)) vertex=cheapest.To;
                 else vertex=cheapest.From;            
                 mst.AddEdge(cheapest);
             }
        }
        return mst;
    }
}
