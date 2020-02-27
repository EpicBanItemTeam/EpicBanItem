package team.ebi.gradle

import com.google.gson.Gson
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar


class TaskOreDeploy extends DefaultTask {
    String pluginId;
    String apiKey;
    Map<String, ?> pluginInfo;
    private String jarTask;

    void setJarTask(String s) {
        jarTask = s
        dependsOn(s)
    }

    @TaskAction
    void deploy() {
        Gson gson = new Gson();
        Jar jar = project.getTasks().getByName(jarTask) as Jar
        File file = jar.getArchivePath()
        HttpClient client = HttpClients.createMinimal()
        HttpPost getSession = new HttpPost("https://ore.spongepowered.org/api/v2/authenticate")
        getSession.setHeader("Content-Type", "application/json;charset=UTF-8");
        getSession.addHeader("Authorization", "OreApi apikey=$apiKey")
        HttpResponse response = client.execute(getSession)
        if (response.statusLine.statusCode != 200) {
            throw new IllegalStateException("Api session missing, invalid or expired")
        }
        ApiSession session = gson.fromJson(EntityUtils.toString(response.getEntity(), "UTF-8"), ApiSession.class)
        HttpPost createNewVersion = new HttpPost("https://ore.spongepowered.org/api/v2/projects/$pluginId/versions")
        String random = UUID.randomUUID().toString();
        createNewVersion.addHeader("Authorization", "OreApi session=$session.session")
        createNewVersion.addHeader("Content-Type", "multipart/form-data; boundary=$random")
        String pluginInfoString = gson.toJson(pluginInfo)
        HttpEntity entity = MultipartEntityBuilder.create()
            .addTextBody("plugin-info", pluginInfoString, ContentType.APPLICATION_JSON)
            .addBinaryBody("plugin-file", file, ContentType.DEFAULT_BINARY, file.getName())
            .setBoundary(random)
            .build()
        createNewVersion.setEntity(entity)
        response = client.execute(createNewVersion)
        String r = EntityUtils.toString(response.getEntity())
        if (response.statusLine.statusCode != 201) {
            throw new RuntimeException(r)
        } else {
            println(r)
        }
    }

}
