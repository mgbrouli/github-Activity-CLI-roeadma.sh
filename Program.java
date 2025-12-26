import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Program{

    public static void main(String[] args) throws IOException {
        

        if(args == null){
            throw new IOException("Arguments is empty");
        }

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.github.com/users/"+ args[0] + "/events"))
        .header("User-Agent", "java-HttpClient")
        .GET()
        .build();


       /* client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenApply(HttpResponse::body)
        .thenAccept(System.out::println)
        .join();*/


        Pattern eventPattern = Pattern.compile("\"type\":\"(.*?)\".*?\"repo\":\\{\"id\":\\d+,\"name\":\"(.*?)\"(?:.*?\"size\":(\\d+))?");
        System.out.println("=".repeat(30));

        try{
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() != 200){
                System.out.println("Api error: " + response.statusCode());
                return;
            }
            String body = response.body();
            Matcher matcher = eventPattern.matcher(body);

            boolean finded = false;
            while (matcher.find()) {
                finded = true;
                String eventType = matcher.group(1);
                String eventRepo = matcher.group(2);
                String eventCommits = matcher.group(3);

                int commits = 0;
                if(eventCommits != null){
                    commits = Integer.parseInt(eventCommits);
                }


                System.out.println("-Event: " + eventType);
                
                formatOutput(eventType, eventRepo, commits);
            }
            if(!finded){
                System.out.println("Don't exist public events or invalid user");
            }


            System.out.println("=".repeat(30));


        }catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }

   private static void formatOutput(String type, String repo, int commits) {
        switch (type) {
            case "PushEvent":
                System.out.println("- Pushed " + (commits > 0 ? commits : 1) + " commit(s) to " + repo);
                break;
            case "IssuesEvent":
                System.out.println("- Opened a new issue in " + repo);
                break;
            case "WatchEvent":
                System.out.println("- Starred " + repo);
                break;
            case "CreateEvent":
                System.out.println("- Created a repository/branch in " + repo);
                break;
            default:
                String cleanName = type.replace("Event", "");
                System.out.println("- " + cleanName + " in " + repo);
                break;
        }
    }

}
