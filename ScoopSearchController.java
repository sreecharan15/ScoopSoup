package com.scoopsoup.controllers;


import com.scoopsoup.dao.ScoopSoupDao;
import com.scoopsoup.model.Scoops;
import com.scoopsoup.repository.ScoopRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/scoops")
public class ScoopSearchController {

    private final ScoopSoupDao scoopSoupDao;

    @Autowired
    private ScoopSearchController(ScoopSoupDao scoopSoupDao) {
        this.scoopSoupDao = scoopSoupDao;
    }

    @Autowired
    private ScoopRepository repository;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Page<Scoops> getAllScoops() {
        return repository.findAll(new PageRequest(0,100));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Scoops getScoopById(@PathVariable("id") ObjectId id) {
        return repository.findBy_id(id);
    }
    @RequestMapping(value = "/verify", method = RequestMethod.POST)
    public int verifyNews(@Valid @RequestBody String scoopText) {
        return scoopSoupDao.KNNModifiedClassifier(scoopText,100);
    }
}