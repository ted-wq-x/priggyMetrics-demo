package wangqiang.website.dao;

import org.springframework.stereotype.Repository;
import wangqiang.website.domain.User;
import org.springframework.data.repository.CrudRepository;
/**
 * Created by wangq on 2017/4/25.
 */
@Repository
public interface UserRepository extends CrudRepository<User, String> {
}
