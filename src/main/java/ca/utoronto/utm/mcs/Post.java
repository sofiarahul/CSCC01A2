package ca.utoronto.utm.mcs;

import static com.mongodb.client.model.Filters.eq;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class Post implements HttpHandler {
	
	
	private MongoClient mongoclient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
	private MongoDatabase database = mongoclient.getDatabase("csc301a2");
	private MongoCollection<Document> collection = database.getCollection("posts");
	
	@Inject 
	public MongoCollection getCollection () {
        return collection;
    }
	
	private void setDatabaseAndCollection (String dbName, String collName){

        MongoDatabase database = mongoclient.getDatabase(dbName); // csc301a2
        MongoCollection<Document> collection = database.getCollection(collName); // posts

    }
	
	public void handle(HttpExchange r)
	{
		try {
			if(r.getRequestMethod().equals("PUT"))
			{
				handlePut(r);
			}
			else if(r.getRequestMethod().equals("GET"))
			{
				handleGet(r);
			}
			else if(r.getRequestMethod().equals("DELETE"))
			{
				handleDelete(r);
			}
			else
			{
				String response = "Method Not Allowed";
				r.sendResponseHeaders(405, response.length());
				OutputStream os = r.getResponseBody();
				os.write(response.getBytes());
				os.close();
				return;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void handlePut(HttpExchange r) throws IOException, JSONException
	{
		try {
			String body = Utils.convert(r.getRequestBody());
			JSONObject deserialized = new JSONObject(body);
			String title = new String("");
			String author = new String("");
			String content = new String("");
			String[] tags;
			JSONArray temp = new JSONArray();
			
			if(deserialized.has("title"))
			{
				//System.out.println("Checking for title");
				title = deserialized.getString("title");
			}
			else
			{
				String response = "Improper format";
				r.sendResponseHeaders(400, response.length());
				OutputStream os = r.getResponseBody();
				os.write(response.getBytes());
				os.close();
				return;
			}
			
			if(deserialized.has("author"))
			{
				//System.out.println("Checking for author");
				author = deserialized.getString("author");
			}
			else
			{
				String response = "Improper format";
				r.sendResponseHeaders(400, response.length());
				OutputStream os = r.getResponseBody();
				os.write(response.getBytes());
				os.close();
				return;
			}
			
			if(deserialized.has("content"))
			{
				//System.out.println("Checking for content");
				content = deserialized.getString("content");
			}
			else
			{
				String response = "Improper format";
				r.sendResponseHeaders(400, response.length());
				OutputStream os = r.getResponseBody();
				os.write(response.getBytes());
				os.close();
				return;
			}
			
			if(deserialized.has("tags"))
			{
				//System.out.println("Before putting tags into temp");
				temp = deserialized.getJSONArray("tags");
				//System.out.println("Before putting temp into tags");
				tags = new String[temp.length()];
				for(int i = 0; i<tags.length; i++)
				{
					tags[i] = temp.optString(i);
					//System.out.println(tags[i]);
				}
			}
			else
			{
				String response = "Improper format";
				r.sendResponseHeaders(400, response.length());
				OutputStream os = r.getResponseBody();
				os.write(response.getBytes());
				os.close();
				return;
			}
			
			//System.out.println("Before calling addblogpost()");
			String id = addBlogPost(title, author, content, tags);
			//System.out.println("After calling addblogpost()");
			
			if(id == null)
			{
				String response = "Could not add blog post";
				r.sendResponseHeaders(400, response.length());
				OutputStream os = r.getResponseBody();
				os.write(response.getBytes());
				os.close();
				return;	
			}
			
			String response = new JSONObject().put("_id",id).toString(); //come back and fix response body
			r.sendResponseHeaders(200, response.length());
			OutputStream os = r.getResponseBody();
			os.write(response.getBytes());
			os.close();
			
		}
		catch(Exception e)
		{
			String response = "Java Exception";
			r.sendResponseHeaders(500, response.length());
			OutputStream os = r.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
		
	}
	
	public void handleDelete(HttpExchange r) throws IOException, JSONException
	{
		try {
			String body = Utils.convert(r.getRequestBody());
			JSONObject deserialized = new JSONObject(body);
			String id = new String("");
			
			if(deserialized.has("_id"))
			{
				//System.out.println("Getting id");
				id = deserialized.getString("_id");
			}
			else
			{
				String response = "Improper format";
				r.sendResponseHeaders(400, response.length());
				OutputStream os = r.getResponseBody();
				os.write(response.getBytes());
				os.close();
				return;
			}
			
			//System.out.println("Before calling deleteblogpost()");
			boolean check = deleteBlogPost(id);
			//System.out.println("After calling deleteblogpost()");
			
			if(check == false)
			{
				//String response = "Could not delete blog post";
				r.sendResponseHeaders(400, 0);
				//r.sendResponseHeaders(400);
				//OutputStream os = r.getResponseBody();
				//os.write(response.getBytes());
				//os.close();
				return;	
			}
			
			//String response = "Successful Deletion";
			r.sendResponseHeaders(200, 0);
			OutputStream os = r.getResponseBody();
			//os.write(response.getBytes());
			os.close();
			
		}
		catch(Exception e)
		{
			String response = "Java Exception";
			r.sendResponseHeaders(500, response.length());
			OutputStream os = r.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
		
	}
	
	public void handleGet(HttpExchange r) throws IOException, JSONException {
		try {
			String body = Utils.convert(r.getRequestBody());
			JSONObject deserialized = new JSONObject(body);
			String title = "";
			String _id = "";
			
			if(deserialized.has("title") && deserialized.has("_id")) {
				title = deserialized.getString("title");
				_id = deserialized.getString("_id");
			}
			else if(deserialized.has("_id")) {
				_id = deserialized.getString("_id");
			} 
			else if(deserialized.has("title"))
			{
				title = deserialized.getString("title");
			}
			else {
				//JSONObject obj = new JSONObject();
				//String response = obj.toString();
				r.sendResponseHeaders(400, 0);
				OutputStream os = r.getResponseBody();
				//os.write(response.getBytes());
				os.close();
			}
			
			if (title != "" &&  _id == "") { //ONLY TITLE
				List<Document> blogPosts = getBlogPostByTitle(title);
				JSONArray posts = new JSONArray();
				
				
				for(int i = 0; i<blogPosts.size(); i++)
				{
					posts = posts.put(new JSONObject(blogPosts.get(i).toJson()));
				}
				
				//obj.put("_id", blogPost.id);
				String response = posts.toString();
				
				if (!blogPosts.isEmpty()) {
					r.sendResponseHeaders(200, response.length());
					OutputStream os = r.getResponseBody();
					os.write(response.getBytes());
					os.close();
				} else {
					r.sendResponseHeaders(404, 0);
					OutputStream os = r.getResponseBody();
					//os.write(response.getBytes());
					os.close();
					return;
				}
			} else if (title == "" && _id != "") { //BY ID
				List<Document> blogPosts = getBlogPostById(_id);
				JSONArray posts = new JSONArray();
				
				for(int i = 0; i<blogPosts.size(); i++)
				{
					posts = posts.put(new JSONObject(blogPosts.get(i).toJson()));
				}
				
				String response = posts.toString();
				
				if (!blogPosts.isEmpty()) {
					r.sendResponseHeaders(200, response.length());
					OutputStream os = r.getResponseBody();
					os.write(response.getBytes());
					os.close();
				} else {
					r.sendResponseHeaders(404, 0);
					OutputStream os = r.getResponseBody();
					//os.write(response.getBytes());
					os.close();
					return;
				}
			}
			else if(title !="" && _id != "") { //ID AND TITLE
				List<Document> blogPosts = getBlogPostByIdAndTitle(_id, title);
				JSONArray posts = new JSONArray();
				
				for(int i = 0; i<blogPosts.size(); i++)
				{
					posts = posts.put(new JSONObject(blogPosts.get(i).toJson()));
				}
				
				String response = posts.toString();
				
				if (!blogPosts.isEmpty()) {
					r.sendResponseHeaders(200, response.length());
					OutputStream os = r.getResponseBody();
					os.write(response.getBytes());
					os.close();
				} else {
					r.sendResponseHeaders(404, 0);
					OutputStream os = r.getResponseBody();
					os.write(response.getBytes());
					os.close();
					return;
				}
			}
			
		} catch (Exception e) {
			//JSONObject obj = new JSONObject();
			//String response = obj.toString();
			r.sendResponseHeaders(500, 0);
			OutputStream os = r.getResponseBody();
			//os.write(response.getBytes());
			os.close();
		}
	}
	
	public String addBlogPost(String title, String author, String content, String[] tags) throws JSONException
	{
		Document checker = null;
		List<String> tags1 = new ArrayList<String>();
		//System.out.println("Before generating the id");
		//ObjectId id = new ObjectId();
		
		//System.out.println("Before creating the blogpost document");
		Document blogPost = new Document();
		blogPost.append("title", title)
				.append("author",author)
				.append("content",content);
		
		for(int i = 0; i<tags.length;i++)
		{
			tags1.add(tags[i]);
			//System.out.println(tags1.get(i));
		}
		
		//System.out.println("Before insertion");
		collection.insertOne(blogPost);
		checker = collection.find(eq("_id", blogPost.get("_id"))).first();
		//System.out.println(checker);
		collection.findOneAndUpdate(Filters.eq("_id", checker.get("_id")), Updates.pushEach("tags", tags1));
		return blogPost.get("_id").toString();
	}
	
	public boolean deleteBlogPost(String _id)
	{
		collection.deleteOne(new Document("_id", new ObjectId(_id)));
		return true;
	}
	
	public List<Document> getBlogPostByTitle(String title)
	{	
		List<Document> blogPosts = collection.find(eq("title",title)).into(new ArrayList<>());
		return blogPosts;
	}
	
	public List<Document> getBlogPostById(String id) 
	{
		List<Document> blogPost = collection.find(eq("_id", new ObjectId(id))).into(new ArrayList<>());
		return blogPost;
	}
	
	public List<Document> getBlogPostByIdAndTitle(String _id, String title)
	{
		List<Document> blogPost = collection.find(eq("_id", new ObjectId(_id))).into(new ArrayList<>());
		if(blogPost.get(0).containsValue(title))
		{
			//System.out.println("Checked if title corresponded");
			return blogPost;
		}
		else
		{
			blogPost.remove(0);
			return blogPost;
		}
	}
	
	

}
