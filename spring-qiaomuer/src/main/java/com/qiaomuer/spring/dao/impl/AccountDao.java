package com.qiaomuer.spring.dao.impl;

import com.qiaomuer.spring.dao.BaseDao;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 类详细描述
 * </p>
 * @author qiaomuer
 * @since 1.0
 */
@Component
public class AccountDao implements BaseDao {
	@Override
	public void validate() {
		System.out.println("AccountDao----validate");
	}
}
