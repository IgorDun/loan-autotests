package dev.kozhevnikov.autotests.web.elements;

import io.qameta.atlas.core.Atlas;
import io.qameta.atlas.webdriver.ElementsCollection;
import io.qameta.atlas.webdriver.extension.FindBy;
import io.qameta.atlas.webdriver.extension.Name;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface TableElement extends BaseElement {

    @Name("Строки таблицы")
    @FindBy(value = ".//div[@data-component='TableListItems']/div |" +
            ".//*[contains(@class,'ant-table-row')]")
    ElementsCollection<TableRowElement> tableRows();

    @Name("Заголовок таблицы")
    @FindBy(value = ".//div[@data-component='TableListCaptions'] | .//*[@class='ant-table-thead']")
    TableRowElement headerRow();

    default TableRowElement row(int rowIndex) {
        List<TableRowElement> rows = tableRows();
        return rows.stream()
                .filter(row -> rows.indexOf(row) + 1 == rowIndex)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Не найдена строка с номером %d. Строки нумеруются от 1.", rowIndex)));
    }

    default TableRowElement rowContaining(int columnIndex, String valueToSearch) {
        return tableRows().stream()
                .filter(row -> {
                    WebElement webElement = row.cell(columnIndex).getWrappedElement();
                    return webElement.getText().trim().contains(valueToSearch)
                            || Optional.ofNullable(webElement.getAttribute("value"))
                            .orElse("").trim().contains(valueToSearch);
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Не найдена строка со значением '%s' в столбце %d. Столбцы нумеруются от 1.", valueToSearch, columnIndex)));
    }

    default TableRowElement rowContaining(String columnName, String valueToSearch) {
        int columnIndex = getColumnIndex(columnName);
        return rowContaining(columnIndex, valueToSearch);
    }

    default int getRowIndex(String columnName, String valueToSearch) {
        int columnIndex = getColumnIndex(columnName);
        for (int i = 0; i < tableRows().size(); i++) {
            if (tableRows().get(i).cell(columnIndex).getWrappedElement().getText().trim().equals(valueToSearch)) {
                return i;
            }
        }
        throw new RuntimeException(String.format("Не найдена строка со значением '%s' в столбце %d. Столбцы нумеруются от 1.", valueToSearch, columnIndex));
    }

    default boolean hasRow(int rowIndex) {
        try {
            row(rowIndex);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    default boolean hasRowContaining(int columnIndex, String valueToSearch) {
        try {
            rowContaining(columnIndex, valueToSearch);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    default boolean hasRowContaining(String columnName, String valueToSearch) {
       try {
           rowContaining(columnName, valueToSearch);
           return true;
       } catch (Exception e) {
           return false;
       }
    }

    default int getColumnIndex(String columnName) {
        List<TableCellElement> headerCells = headerRow().cells();
        TableCellElement targetCell = headerCells.stream()
                .filter(cell -> cell.getWrappedElement().getText().trim().contains(columnName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Не найдена ячейка со значением '%s' в заголовке таблицы", columnName)));
        return headerCells.indexOf(targetCell) + 1;
    }

    interface TableRowElement extends BaseElement {

        @Name("Ячейки таблицы")
        @FindBy(value = ".//div[@cellpadding] | .//*[contains(@class,'ant-table-cell')]")
        ElementsCollection<TableCellElement> cells();

        default TableCellElement cell(int columnIndex) {
            try {
                return cells().get(columnIndex - 1);
            } catch (IndexOutOfBoundsException e) {
                throw new RuntimeException(
                        String.format("Не найдена ячейка в столбце %d. Столбцы нумеруются от 1.", columnIndex), e
                );
            }
        }

        default TableCellElement cellContaining(int columnIndex, String valueToSearch) {
            List<TableCellElement> cells = cells();
            return cells().stream()
                    .filter(cell -> (cells.indexOf(cell) + 1) == columnIndex || cell.getWrappedElement().getText().trim().contains(valueToSearch))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(String.format("Не найдена ячейка со значением '%s' в столбце %d. Столбцы нумеруются от 1.", valueToSearch, columnIndex)));
        }

        default TableCellElement cellContaining(String columnName, String valueToSearch, Atlas atlas) {
            List<WebElement> precedingRows = getWrappedElement().findElements(By.xpath(".//ancestor::div//div[@data-component='TableListCaptions'] | .//ancestor::div//*[@class='ant-table-thead']"));
            List<WebElement> followingRows = getWrappedElement().findElements(By.xpath(String.format(".//following-sibling::%s", getWrappedElement().getTagName())));
            List<WebElement> rows = new ArrayList<>(precedingRows);
            rows.add(getWrappedElement());
            rows.addAll(followingRows);

            List<TableCellElement> headerCells = atlas.create(rows.get(0), TableRowElement.class).cells();
            TableCellElement targetCell = headerCells.stream()
                    .filter(cell -> cell.getWrappedElement().getText().trim().startsWith(columnName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(String.format("Не найдена ячейка со значением '%s' в столбце %s.", valueToSearch, columnName)));
            int columnIndex = headerCells.indexOf(targetCell) + 1;
            return cellContaining(columnIndex, valueToSearch);
        }

        default TableCellElement cellContaining(String valueToSearch) {
            return cells().stream()
                    .filter(cell -> cell.getWrappedElement().getText().trim().contains(valueToSearch))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(String.format("Не найдена ячейка со значением '%s'", valueToSearch)));
        }

        default TableCellElement lastCell() {
            return cell(cells().size());
        }

        default boolean hasCell(int columnIndex) {
            try {
                cell(columnIndex);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        default boolean hasCellContaining(int columnIndex, String valueToSearch) {
            try {
                cellContaining(columnIndex, valueToSearch);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        default boolean hasCellContaining(String columnName, String valueToSearch, Atlas atlas) {
            try {
                cellContaining(columnName, valueToSearch, atlas);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    interface TableCellElement extends BaseElement {

    }
}