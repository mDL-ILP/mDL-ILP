package com.ul.ts.products.mdlreader.data;


import com.ul.ts.products.mdlreader.utils.ParsingUtils;

public class Category {

    // Total categories = 16
    public enum CategoryLabel {
        AM, A1, A2, A,			// 4 Bikes
        B1, B,					// 2 Cars
        C1, C, 					// 2 Trucks
        D1, D, 					// 2 Busses
        BE, C1E, CE, D1E, DE,	// 5 Trailers
        T}						// 1 Tractor

    private CategoryLabel categoryLabel;
    private String fromDate;
    private String untillDate;
    private String restrictions;

    public Category(CategoryLabel label, String fromDate, String untillDate, String restrictions) {
        this.categoryLabel = label;
        this.fromDate = ParsingUtils.fromYYYYtoYY(fromDate);
        this.untillDate = ParsingUtils.fromYYYYtoYY(untillDate);
        this.restrictions = restrictions;
    }

    public CategoryLabel getLabel() {
        return this.categoryLabel;
    }

    public String getLabelAsString() {
        switch (categoryLabel) {
            case AM:	return "AM";
            case A1:	return "A1";
            case A2:	return "A2";
            case A:		return "A";
            case B1:	return "B1";
            case B:		return "B";
            case C1:	return "C1";
            case C:		return "C";
            case D1:	return "D1";
            case D:		return "D";
            case BE:	return "BE";
            case C1E:	return "C1E";
            case CE:	return "CE";
            case D1E:	return "D1E";
            case DE:	return "DE";
            case T:		return "T";
            default:	return "";
        }
    }

    public String getFromDate() {
        if (this.fromDate == null)
            return "-";
        return this.fromDate;
    }

    public String getUntillDate() {
        if (this.untillDate == null)
            return "-";
        return this.untillDate;
    }

    public String getRestrictions() {
        if (this.restrictions == null || this.restrictions.isEmpty())
            return "-";
        return this.restrictions;
    }
}
