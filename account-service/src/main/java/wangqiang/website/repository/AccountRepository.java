package wangqiang.website.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import wangqiang.website.domain.Account;

@Repository
public interface AccountRepository extends CrudRepository<Account, String> {

	Account findByName(String name);

}
