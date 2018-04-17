import core.configuration.App;
import core.configuration.Source;
import core.model.Author;
import core.repository.AuthorRepository;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {App.class, Source.class})
public class AuthorRepositoryTest {
    private static Logger logger = Logger.getLogger(AuthorRepositoryTest.class.getSimpleName());

    @Autowired
    private AuthorRepository repository;

    @Test
    public void selectById() {
        Author author = new Author(); author.setFirstName("TEST"); author.setLastName("TEST");
        Author inserted = repository.insert(author);
        Author selected = repository.select(inserted.getId());
        assertEquals(String.format("Author{id=%d, firstName='TEST', lastName='TEST'}", selected.getId()), selected.toString());
    }

    @Test
    public void selectByIdNull() {
        Author author = repository.select(0L);
        assertEquals(new Author(), author);
    }

    @Test
    public void selectAll() {
        List<Author> authors = (List<Author>) repository.select();
        assertEquals(0, authors.size());
        Author author = new Author(); author.setFirstName("TEST"); author.setLastName("TEST");
        repository.insert(author);
        authors = (List<Author>) repository.select();
        assertEquals(1, authors.size());
    }

    @Test
    public void insert() {
        Author author = new Author(); author.setFirstName("TEST"); author.setLastName("TEST");
        Author result = repository.insert(author);

        assertTrue(result.getId() > 0);
    }

    @Test
    public void insertAll() {
        Author author1 = new Author(); author1.setFirstName("a1"); author1.setLastName("a1");
        Author author2 = new Author(); author2.setFirstName("a2"); author2.setLastName("a2");

        List<Author> authors = (List<Author>) repository.insert(Arrays.asList(author1, author2));

        assertTrue(authors.stream().allMatch(a -> a.getId() > 0));
    }

    @Test
    public void update() {
        Author author = new Author(); author.setFirstName("TEST"); author.setLastName("TEST");
        Author forUpdate = repository.insert(author);
        forUpdate.setFirstName("UPDATED");
        repository.update(forUpdate);
        Author updated = repository.select(forUpdate.getId());

        assertEquals(String.format("Author{id=%d, firstName='UPDATED', lastName='TEST'}", updated.getId()), updated.toString());
    }

    @Test
    public void updateAll() {
        Author authorOne = new Author(); authorOne.setFirstName("A1"); authorOne.setLastName("B1");
        Author authorTwo = new Author(); authorTwo.setFirstName("A2"); authorTwo.setLastName("B2");

        Iterable<Author> authors = repository.insert(Arrays.asList(authorOne, authorTwo));
        authorOne.setFirstName("UPDATED");
        authorTwo.setLastName("UPDATED");
        repository.update(authors);

        assertEquals(String.format("Author{id=%d, firstName='UPDATED', lastName='B1'}", authorOne.getId()), authorOne.toString());
        assertEquals(String.format("Author{id=%d, firstName='A2', lastName='UPDATED'}", authorTwo.getId()), authorTwo.toString());
    }

    @Test
    public void deleteById() {
        Author author = new Author(); author.setFirstName("TEST"); author.setLastName("TEST");
        repository.insert(author);

        assertTrue(author.getId() > 0);
        assertEquals(1, ((List<Author>)repository.select()).size());

        repository.deleteById(author.getId());
        assertEquals(0, ((List<Author>)repository.select()).size());
    }

    @Test
    public void deleteByIdAll() {
        Author authorOne = new Author(); authorOne.setFirstName("A1"); authorOne.setLastName("B1");
        Author authorTwo = new Author(); authorTwo.setFirstName("A2"); authorTwo.setLastName("B2");
        List<Author> authors = (List<Author>) repository.insert(Arrays.asList(authorOne, authorTwo));

        assertTrue(authors.stream().allMatch(x -> x.getId() > 0));
        assertEquals(2, ((List<Author>)repository.select()).size());

        repository.deleteById(authors.stream().map(Author::getId).collect(Collectors.toList()));
        assertEquals(0, ((List<Author>)repository.select()).size());
    }

    @Test
    public void delete() {
        Author author = new Author(); author.setFirstName("TEST"); author.setLastName("TEST");
        repository.insert(author);

        assertTrue(author.getId() > 0);
        assertEquals(1, ((List<Author>)repository.select()).size());

        repository.delete(author);
        assertEquals(0, ((List<Author>)repository.select()).size());
    }

    @Test
    public void deleteAll() {
        Author authorOne = new Author(); authorOne.setFirstName("A1"); authorOne.setLastName("B1");
        Author authorTwo = new Author(); authorTwo.setFirstName("A2"); authorTwo.setLastName("B2");
        List<Author> authors = (List<Author>) repository.insert(Arrays.asList(authorOne, authorTwo));

        assertTrue(authors.stream().allMatch(x -> x.getId() > 0));
        assertEquals(2, ((List<Author>)repository.select()).size());

        repository.delete(authors);
        assertEquals(0, ((List<Author>)repository.select()).size());
    }
}
