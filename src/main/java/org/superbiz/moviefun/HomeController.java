package org.superbiz.moviefun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

import java.util.Map;

@Controller
public class HomeController {

    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;

    private final TransactionTemplate albumsTemplate;
    private final TransactionTemplate moviesTemplate;
    private PlatformTransactionManager albumsTM;
    private PlatformTransactionManager moviesTM;

    public HomeController(MoviesBean moviesBean,
                          AlbumsBean albumsBean,
                          MovieFixtures movieFixtures,
                          AlbumFixtures albumFixtures,
                          @Autowired @Qualifier("albums-TransactionManager") PlatformTransactionManager albumsTM,
                          @Autowired @Qualifier("movies-TransactionManager") PlatformTransactionManager moviesTM) {
        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;

        this.albumsTM = albumsTM;
        this.moviesTM = moviesTM;

        this.albumsTemplate = new TransactionTemplate(this.albumsTM);
        this.moviesTemplate = new TransactionTemplate(this.moviesTM);
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/setup")
    public String setup(Map<String, Object> model) {

       this.moviesTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                for (Movie movie : movieFixtures.load()) {
                    moviesBean.addMovie(movie);
                }
            }
        });

        this.albumsTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                for (Album album : albumFixtures.load()) {
                    albumsBean.addAlbum(album);
                }
            }
        });

        model.put("movies", moviesBean.getMovies());
        model.put("albums", albumsBean.getAlbums());

        return "setup";
    }
}
