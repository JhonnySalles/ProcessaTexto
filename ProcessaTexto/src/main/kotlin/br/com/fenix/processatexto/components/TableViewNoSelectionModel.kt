package br.com.fenix.processatexto.components

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.TableColumn
import javafx.scene.control.TablePosition
import javafx.scene.control.TableView


class TableViewNoSelectionModel<T>(tableView: TableView<T>) : TableView.TableViewSelectionModel<T>(tableView) {

    override fun getSelectedCells(): ObservableList<TablePosition<Any, Any>> = FXCollections.emptyObservableList()

    @Override
    override fun selectLeftCell() {
    }

    @Override
    override fun selectRightCell() {
    }

    @Override
    override fun selectAboveCell() {
    }

    @Override
    override fun selectBelowCell() {
    }

    @Override
    override fun clearSelection(i: Int, tableColumn: TableColumn<T, *>) {
    }

    @Override
    override fun clearAndSelect(i: Int, tableColumn: TableColumn<T, *>) {
    }

    @Override
    override fun select(i: Int, tableColumn: TableColumn<T, *>) {
    }

    @Override
    override fun isSelected(i: Int, tableColumn: TableColumn<T, *>): Boolean {
        return false
    }

    override fun getSelectedIndices() = FXCollections.emptyObservableList<Int>()

    override fun getSelectedItems() = FXCollections.emptyObservableList<T>()

    @Override
    override fun selectIndices(i: Int, vararg ints: Int) {
    }

    @Override
    override fun selectAll() {
    }

    @Override
    override fun clearAndSelect(i: Int) {
    }

    @Override
    override fun select(i: Int) {
    }

    @Override
    override fun select(obj: T) {
    }

    @Override
    override fun clearSelection(i: Int) {
    }

    @Override
    override fun clearSelection() {
    }

    @Override
    override fun isSelected(i: Int): Boolean {
        return false
    }

    override fun isEmpty() = false

    @Override
    override fun selectPrevious() {
    }

    @Override
    override fun selectNext() {
    }

    @Override
    override fun selectFirst() {
    }

    @Override
    override fun selectLast() {
    }
}