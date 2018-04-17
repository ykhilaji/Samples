package core.model;

import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Author.class)
public class Author_ {
    public static volatile SingularAttribute<Author, Long> id;
    public static volatile SingularAttribute<Author, String> firstName;
    public static volatile SingularAttribute<Author, Integer> lastName;
    public static volatile ListAttribute<Author, Book> books;
}
