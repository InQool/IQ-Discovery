package com.inqool.dcap.office.api.resource;

import com.inqool.dcap.common.entity.Article;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.office.api.core.ArticleAccess;
import com.inqool.dcap.security.ZdoRoles;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.picketlink.authorization.annotations.LoggedIn;
import org.picketlink.authorization.annotations.RolesAllowed;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@Path("/article")
@LoggedIn
@RolesAllowed(ZdoRoles.REDACTOR)
@RequestScoped
public class ArticleRS {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private ArticleAccess articleAccess;

    @Inject
    private DataStore store;

    @Path("/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listArticles() {
        try {
            List<Article> articles = articleAccess.listArticles();
            return Response.ok(articles).build();
        } catch (Exception e) {
            logger.error("Failed while getting article list.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createNewArticle(Article article) {
        try {
            articleAccess.createArticle(article);
            return Response.ok(article).build();
        } catch (Exception e) {
            logger.error("Failed while creating article.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/{articleId}/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getArticle(@PathParam("articleId") int articleId) {
        try {
            Article article = articleAccess.fetchArticle(articleId);
            return Response.ok(article).build();
        } catch(Exception e) {
            logger.error("Could not get article.", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/{articleId}/")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateArticle(Article article, @PathParam("articleId") int articleId) {
        try {
            articleAccess.updateArticle(articleId, article);
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not update article.", e);
            return Response.serverError().build();
        }
    }

    @Path("/{articleId}/image")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadArticleImage(@PathParam("articleId") int articleId, MultipartFormDataInput input) {
        try {
            store.startTransaction();
            articleAccess.uploadImage(articleId, input);
            store.commitTransaction();
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not update article image.", e);
            store.rollbackTransaction();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/{articleId}/image/")
    @DELETE
    public Response deleteArticleImage(@PathParam("articleId") int articleId) {
        try {
            store.startTransaction();
            articleAccess.deleteImage(articleId);
            store.commitTransaction();
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not delete article image.", e);
            store.rollbackTransaction();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("/delete/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteArticles(List<Integer> articleIds) {
        try {
            store.startTransaction();
            articleAccess.deleteArticles(articleIds);
            store.commitTransaction();
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not delete articles.", e);
            store.rollbackTransaction();
            return Response.serverError().build();
        }
    }

    @Path("/{articleId}/")
    @DELETE
    public Response deleteArticle(@PathParam("articleId") int articleId) {
        try {
            store.startTransaction();
            articleAccess.deleteArticle(articleId);
            store.commitTransaction();
            return Response.ok().build();
        } catch(Exception e) {
            logger.error("Could not delete article.", e);
            store.rollbackTransaction();
            return Response.serverError().build();
        }
    }
}
