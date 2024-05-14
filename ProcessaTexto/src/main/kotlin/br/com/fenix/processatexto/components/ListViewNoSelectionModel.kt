package br.com.fenix.processatexto.components

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.MultipleSelectionModel


class ListViewNoSelectionModel<T> : MultipleSelectionModel<T>() {

    override fun getSelectedIndices() = FXCollections.emptyObservableList<Int>()

    override fun getSelectedItems() = FXCollections.emptyObservableList<T>()

    @Override
    override fun selectIndices(index: Int, vararg indices: Int) {
    }

    @Override
    override fun selectAll() {
    }

    @Override
    override fun selectFirst() {
    }

    @Override
    override fun selectLast() {
    }

    @Override
    override fun clearAndSelect(index: Int) {
    }

    @Override
    override fun select(index: Int) {
    }

    @Override
    override fun select(obj: T) {
    }

    @Override
    override fun clearSelection(index: Int) {
    }

    @Override
    override fun clearSelection() {
    }

    @Override
    override fun isSelected(index: Int): Boolean {
        return false
    }

    override fun isEmpty() = true

    @Override
    override fun selectPrevious() {
    }

    @Override
    override fun selectNext() {
    }
}