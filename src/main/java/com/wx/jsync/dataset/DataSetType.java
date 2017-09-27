package com.wx.jsync.dataset;

import com.wx.jsync.dataset.factory.DataSetFactory;
import com.wx.jsync.dataset.factory.impl.GDriveDataSetFactory;
import com.wx.jsync.dataset.factory.impl.LocalDataSetFactory;

public enum DataSetType {
    LOCAL(new LocalDataSetFactory()),
    GDRIVE(new GDriveDataSetFactory());

    private final DataSetFactory factory;

    DataSetType(DataSetFactory factory) {
        this.factory = factory;
    }

    public DataSetFactory getFactory() {
        return factory;
    }
}