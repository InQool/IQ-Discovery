package com.inqool.dcap.discovery.api.resource;

import com.inqool.dcap.common.entity.Article;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.discovery.api.core.ArticleAccess;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@Path("/article")
@RequestScoped
public class ArticleRS {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private ArticleAccess articleAccess;

    @Path("/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listArticleHeaders() {
        try {
            List<Article> articles = articleAccess.listPublishedArticleHeaders();
            return Response.ok(articles).build();
        } catch (Exception e) {
            logger.error("Failed while getting article list.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/full")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listArticles() {
        try {
            List<Article> articles = articleAccess.listPublishedArticles();
            return Response.ok(articles).build();
        } catch (Exception e) {
            logger.error("Failed while getting article list.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/{articleId}/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getArticle(@PathParam("articleId") int articleId) {
        try {
            Article article = articleAccess.fetchPublishedArticle(articleId);
            return Response.ok(article).build();
        } catch(Exception e) {
            logger.error("Could not get article.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
