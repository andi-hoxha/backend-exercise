package repositories;

import models.User;
import services.BaseService;

import java.util.concurrent.CompletableFuture;

public interface UserRepository extends BaseRepository<User> {

    CompletableFuture<User> findByUser(String email);
}
