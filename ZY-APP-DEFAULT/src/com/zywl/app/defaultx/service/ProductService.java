package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Product;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService extends DaoService{

	public ProductService( ) {
		super("ProductMapper");
	}




	public List<Product> findAllProduct(){
		return findAll();
	}
}
