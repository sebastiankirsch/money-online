package net.tcc.money.online.server.domain;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.repackaged.com.google.common.base.Function;
import net.tcc.money.online.shared.Constants;
import net.tcc.money.online.shared.dto.Article;

import com.google.appengine.datanucleus.annotations.Unowned;

@PersistenceCapable
public class PersistentArticle extends DataSetBoundEntity implements Serializable {

    private static final long serialVersionUID = Constants.SERIAL_VERSION;

    public static final Function<PersistentArticle, Article> toArticle = new Function<PersistentArticle, Article>() {
        @Nullable
        @Override
        public Article apply(@Nullable PersistentArticle article) {
            return article == null ? null : article.toArticle();
        }
    };

    @Nonnull
    @Persistent(nullValue = NullValue.EXCEPTION)
    private String name;

    @Nullable
    @Persistent
    private String brand;

    @Persistent
    private boolean vegan;

    @Nullable
    @Persistent
    private String lotSize;

    @Nullable
    @Persistent
    @Unowned
    private PersistentCategory category;

    @Deprecated
    @SuppressWarnings({"deprecation", "unused"})
    private PersistentArticle() { // for JDO
        super();
    }

    public PersistentArticle(@Nonnull String dataSetId, @Nonnull String name, @Nullable String brand, boolean vegan,
                             @Nullable String lotSize) {
        super(dataSetId);
        this.name = name;
        this.brand = brand;
        this.vegan = vegan;
        this.lotSize = lotSize;
    }

    @Override
    public String toString() {
        return super.toString() + " [" + this.name + "/" + this.brand + "]";
    }

    @Nonnull
    public Article toArticle() {
        Article article = new Article(getKey(), name, brand, vegan, lotSize);
        PersistentCategory category = this.category;
        article.setCategory(category == null ? null : category.toCategory());
        return article;
    }

    public void setCategory(@Nullable PersistentCategory category) {
        this.category = category;
    }

}
