package core.model;

import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Publisher.class)
public class Publisher_ {
    public static volatile SingularAttribute<Publisher, Long> id;
    public static volatile SingularAttribute<Publisher, String> name;
    public static volatile ListAttribute<Publisher, Book> books;
}
