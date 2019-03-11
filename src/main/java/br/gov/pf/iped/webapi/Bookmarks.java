package br.gov.pf.iped.webapi;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.PATCH;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import dpf.sp.gpinf.indexer.search.IPEDSearcher;
import dpf.sp.gpinf.indexer.search.ItemId;
import dpf.sp.gpinf.indexer.search.MultiMarcadores;
import dpf.sp.gpinf.indexer.search.MultiSearchResult;

@Path("bookmarks")
public class Bookmarks {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getAll() throws Exception{
        
        JSONArray data = new JSONArray();
        Set<String> bookmarks = Sources.multiSource.getMultiMarcadores().getLabelMap(); 
        for (String b : bookmarks) {
            JSONObject inner = new JSONObject();
            inner.put("id", b);
            JSONObject relationships = new JSONObject();
    		inner.put("relationships", relationships);
    		relationships.put("add", "/bookmarks/"+ URLEncoder.encode(b, "UTF-8") +"/add");
    		relationships.put("remove", "/bookmarks/"+ URLEncoder.encode(b, "UTF-8") +"/remove");
    		relationships.put("rename", "/bookmarks/"+ URLEncoder.encode(b, "UTF-8") +"/rename/{new}");
            data.add(inner);
        }
        
        JSONObject json = new JSONObject();
        json.put("data", data);

        return json.toString();
    }
    
    @GET
    @Path("{bookmark}")
    @Produces(MediaType.APPLICATION_JSON)
    public String get(@PathParam("bookmark") String bookmark) throws Exception{
        
        IPEDSearcher searcher = new IPEDSearcher(Sources.multiSource, "");
        MultiSearchResult result = searcher.multiSearch();
        result = Sources.multiSource.getMultiMarcadores().filtrarMarcadores(result, Collections.singleton(bookmark));
        
        JSONArray data = new JSONArray();
        for (ItemId id : result.getIterator()) {
            JSONObject item = new JSONObject();
            item.put("source", id.getSourceId());
            item.put("id", id.getId());
            data.add(item);
        }
        
        JSONObject json = new JSONObject();
        json.put("data", data);

        return json.toString();
    }
    
    @PATCH
    @Path("{bookmark}/add")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response insertLabel(@PathParam("bookmark") String bookmark, String json) throws ParseException {
        MultiMarcadores mm = Sources.multiSource.getMultiMarcadores();
        List<ItemId> itemIds = getItemIdFromJsonArray(json);
        mm.addLabel(itemIds, bookmark);
        mm.saveState();
        return Response.ok().build();
    }
    
    @PATCH
    @Path("{bookmark}/remove")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeLabel(@PathParam("bookmark") String bookmark, String json) throws ParseException {
        MultiMarcadores mm = Sources.multiSource.getMultiMarcadores();
        List<ItemId> itemIds = getItemIdFromJsonArray(json);
        mm.removeLabel(itemIds, bookmark);
        mm.saveState();
        return Response.ok().build();
    }
    
    public static List<ItemId> getItemIdFromJsonArray(String json) throws ParseException{
        JSONArray list = (JSONArray)JSONValue.parseWithException(json);
        List<ItemId> itemIds = new ArrayList<>();
        for(Object o : list){
            JSONObject obj = (JSONObject)o;
            int sourceID = (int)(long)obj.get("source");
            for(Object id : (JSONArray)obj.get("ids")) {
                ItemId item = new ItemId(sourceID, (int)(long)id);
                itemIds.add(item);
            }
        }
        return itemIds;
    }
    
    @POST
    @Path("{bookmark}")
    public Response addLabel(@PathParam("bookmark") String bookmark) {
        MultiMarcadores mm = Sources.multiSource.getMultiMarcadores();
        mm.newLabel(bookmark);
        mm.saveState();
        return Response.ok().build();
    }
    
    @DELETE
    @Path("{bookmark}")
    public Response delLabel(@PathParam("bookmark") String bookmark) {
        MultiMarcadores mm = Sources.multiSource.getMultiMarcadores();
        mm.delLabel(bookmark);
        mm.saveState();
        return Response.ok().build();
    }
    
    @PUT
    @Path("{old}/rename/{new}")
    public Response changeLabel(@PathParam("old") String oldLabel, @PathParam("new") String newLabel) {
        MultiMarcadores mm = Sources.multiSource.getMultiMarcadores();
        mm.changeLabel(oldLabel, newLabel);
        mm.saveState();
        return Response.ok().build();
    }
    
}