package com.inqool.dcap.office.api.core;

import com.inqool.dcap.common.entity.Article;
import com.inqool.dcap.common.entity.QArticle;
import com.inqool.dcap.config.Zdo;
import com.inqool.dcap.integration.model.ZdoFileType;
import com.inqool.dcap.integration.service.DataStore;
import com.inqool.dcap.security.PicketLinkAccess;
import com.inqool.dcap.security.model.ZdoOrganization;
import com.inqool.dcap.security.model.ZdoUser;
import com.mysema.query.jpa.impl.JPAQuery;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

/**
 * @author Lukas Jane (inQool) 5. 6. 2015.
 */
@Transactional
@RequestScoped
public class ArticleAccess {

    @Inject
    @Zdo
    private Logger logger;

    @Inject
    private EntityManager em;

    @Inject
    private DataStore store;

    @Inject
    private ImgUploadTools imgUploadTools;

    @Inject
    private PicketLinkAccess plAccess;

    public List<Article> listArticles() {
        JPAQuery jpaQuery = new JPAQuery(em);
        QArticle qArticle = QArticle.article;
        List<Article> articleList = jpaQuery
                .from(qArticle)
                .where(
                        qArticle.owner.eq(plAccess.getUser().getLoginName())
                                .and(qArticle.deleted.eq(false))
                )
                .list(qArticle);
        return articleList;
    }

    public void createArticle(Article article) {
        ZdoUser author = plAccess.getUser();
        article.setOwner(author.getLoginName());
        author = plAccess.getUser(author.getLoginName());   //Because first author is just from token and has these values empty
        article.setAuthorName(author.getFirstName() + " " + author.getLastName());

        ZdoOrganization org = plAccess.getOrganizationOfUser(author);
        article.setAuthorOrgName(plAccess.removePrispevkovaOrganizaceFromOrgName(org.getDisplayName()));
        em.persist(article);
        em.flush();
    }

    public Article fetchArticle(int articleId) {
        Article article = em.find(Article.class, articleId);
        if(article == null || article.isDeleted()) {
            throw new RuntimeException("Can't get article, article not found.");
        }
        return article;
    }

    public void updateArticle(int articleId, Article updatedArticle) {
        Article dbArticle = em.find(Article.class, articleId);
        if(dbArticle == null) {
            throw new RuntimeException("No such article");
        }
        if(!dbArticle.getOwner().equals(plAccess.getUser().getLoginName())) {
            throw new RuntimeException("The user has no such article.");
        }
        dbArticle.setTitle(updatedArticle.getTitle());
        dbArticle.setPerex(updatedArticle.getPerex());
        dbArticle.setUrl(updatedArticle.getUrl());
        dbArticle.setContent(updatedArticle.getContent());
        dbArticle.setActive(updatedArticle.isActive());
        dbArticle.setPublishedFrom(updatedArticle.getPublishedFrom());
        dbArticle.setPublishedTo(updatedArticle.getPublishedTo());
        dbArticle.setCreated(updatedArticle.getCreated());
    }

    public void deleteArticle(int articleId) throws IOException {
        Article article = em.find(Article.class, articleId);
        if(article == null) {
            throw new RuntimeException("No such article");
        }
        if(!article.getOwner().equals(plAccess.getUser().getLoginName())) {
            throw new RuntimeException("The user has no such article.");
        }
        String imageId = article.getImageId();
        if(imageId != null) {
            store.delete(store.createUrl(imageId));
        }
        article.setImageId(null);
        article.setDeleted(true);
    }

    public void deleteArticles(List<Integer> articleIds) throws IOException {
        for (Integer articleId : articleIds) {
            deleteArticle(articleId);
        }
    }

    public void uploadImage(int articleId, MultipartFormDataInput input) throws IOException {
        Article article = em.find(Article.class, articleId);
        if(article == null) {
            throw new RuntimeException("No such article");
        }
        if(!article.getOwner().equals(plAccess.getUser().getLoginName())) {
            throw new RuntimeException("The user has no such article.");
        }
        String oldImageId = article.getImageId();
        if(oldImageId != null) {
            store.delete(store.createUrl(oldImageId));
        }

        String uuid = imgUploadTools.uploadedFileToFedora(input, ZdoFileType.articleImage);
        article.setImageId(uuid);
    }

    public void deleteImage(int articleId) throws IOException {
        Article article = em.find(Article.class, articleId);
        if(article == null) {
            throw new RuntimeException("No such article");
        }
        if(!article.getOwner().equals(plAccess.getUser().getLoginName())) {
            throw new RuntimeException("The user has no such article.");
        }
        String oldImageId = article.getImageId();
        if(oldImageId != null) {
            store.delete(store.createUrl(oldImageId));
        }
        article.setImageId(null);
    }


}
