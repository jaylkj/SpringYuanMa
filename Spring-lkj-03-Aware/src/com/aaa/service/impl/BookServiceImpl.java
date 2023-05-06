package com.aaa.service.impl;

import com.aaa.dao.BookDao;
import com.aaa.service.BookService;
import com.spring.anno.Autowired;
import com.spring.anno.Component;
import com.spring.anno.Scope;

@Component("bookService")
@Scope("prototype")
public class BookServiceImpl implements BookService {
    @Autowired
    private BookDao bookDao;

    @Override
    public BookDao getBookDao() {
        return bookDao;
    }
}
