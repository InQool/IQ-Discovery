package com.inqool.dcap.discovery.api.core;

import com.inqool.dcap.common.entity.Article;
import com.inqool.dcap.common.entity.QArticle;
import com.inqool.dcap.config.Zdo;
import com.mysema.query.jpa.impl.JPAQuery;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
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

    public Article fetchPublishedArticle(int articleId) {
        Article article = em.find(Article.class, articleId);
        if(article == null || article.isDeleted()) {
            throw new RuntimeException("Can't get article, article not found.");
        }
        if(!article.isActive()
                || (article.getPublishedFrom() != null && article.getPublishedFrom().isAfter(LocalDateTime.now()))
                || (article.getPublishedTo() != null && article.getPublishedTo().isBefore(LocalDateTime.now()))
                ) {
            throw new RuntimeException("This article isn't published.");
        }
        return article;
    }

    public List<Article> listPublishedArticles() {
        JPAQuery jpaQuery = new JPAQuery(em);
        QArticle qArticle = QArticle.article;
        List<Article> articleList = jpaQuery
                .from(qArticle)
                .where(qArticle.deleted.eq(false)
                                .and(qArticle.active.eq(true))
                                .and(qArticle.publishedFrom.isNull().or(qArticle.publishedFrom.before(LocalDateTime.now())))
                                .and(qArticle.publishedTo.isNull().or(qArticle.publishedTo.after(LocalDateTime.now())))
                )
                .orderBy(qArticle.created.desc())
                .list(qArticle);
        return articleList;
    }

    public List<Article> listPublishedArticleHeaders() {
        JPAQuery jpaQuery = new JPAQuery(em);
        QArticle qArticle = QArticle.article;
        List<Article> articleList = jpaQuery
                .from(qArticle)
                .where(qArticle.deleted.eq(false)
                                .and(qArticle.active.eq(true))
                                .and(qArticle.publishedFrom.isNull().or(qArticle.publishedFrom.before(LocalDateTime.now())))
                                .and(qArticle.publishedTo.isNull().or(qArticle.publishedTo.after(LocalDateTime.now())))
                                )
                        .orderBy(qArticle.created.desc())
                .list(qArticle);
        return articleList;
    }
}
