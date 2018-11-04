package com.uclv.gui;

import java.beans.PropertyChangeEvent;

/**
 * Created by anonymous on 1/6/2018.
 */
public class LabelPropertyChange extends PropertyChangeEvent {

    private int amount;

    /**
     * Constructs a new {@code PropertyChangeEvent}.
     *
     * @param source       the bean that fired the event
     * @param propertyName the programmatic name of the property that was changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     * @throws IllegalArgumentException if {@code source} is {@code null}
     */
    public LabelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        super(source, propertyName, oldValue, newValue);

    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

}
