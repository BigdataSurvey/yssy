package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserBook;
import com.zywl.app.base.bean.card.Card;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserBookService extends DaoService{

	public UserBookService( ) {
		super("UserBookMapper");
	}


	
	public List<UserBook> findAllCard(){
		return findAll();
	}


	@Transactional
	public void addUserBook(Long userId,Long itemId,int number){
		UserBook userBook = new UserBook();
		userBook.setUserId(userId);
		userBook.setItemId(itemId);
		userBook.setAllNumber(number);
		userBook.setTodayNumber(number);
		userBook.setCanReceive(BigDecimal.ZERO);
		userBook.setNumber(0);
		userBook.setSettleTime(new Date());
		userBook.setAddTime(new Date());
		userBook.setUnlockTime(DateUtil.getDateByDay(1));
		save(userBook);
	}

	public UserBook findByUserIdAndItemId(Long userId,Long itemId){
		Map<String,Object> map = new HashMap<>();
		map.put("userId",userId);
		map.put("itemId",itemId);
		return (UserBook) findOne("findByUserIdAndItemId",map);
	}

	public List<UserBook> findByUserId(Long userId){
		Map<String,Object> map = new HashMap<>();
		map.put("userId",userId);
		return findList("findByUserId",map);
	}

	@Transactional
	public void deleteBook(Long userId,Long itemId){
		Map<String,Object> map = new HashMap<>();
		map.put("userId",userId);
		map.put("itemId",itemId);
		execute("deleteBook",map);
	}


	public void updateUserBook(UserBook userBook){
		execute("updateUserBook",userBook);
	}

	@Transactional
	public void batchUpdate(List<UserBook> userBooks){
		if (userBooks.size()>0){
			execute("batchUpdateBook",userBooks);
		}
	}

}
