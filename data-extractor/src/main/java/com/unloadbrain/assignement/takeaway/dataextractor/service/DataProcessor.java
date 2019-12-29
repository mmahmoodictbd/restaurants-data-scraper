package com.unloadbrain.assignement.takeaway.dataextractor.service;

public abstract class DataProcessor<IN, OUT> {

    public abstract OUT processor(IN input);
}
