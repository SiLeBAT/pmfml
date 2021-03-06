/***************************************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors: Department Biological Safety - BfR
 **************************************************************************************************/
package de.bund.bfr.pmfml.sbml;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Miguel Alba
 */
public class ReferenceImpl implements Reference {

    private static final String AUTHOR = "author";
    private static final String YEAR = "year";
    private static final String TITLE = "title";
    private static final String ABSTRACT_TEXT = "abstractText";
    private static final String JOURNAL = "journal";
    private static final String VOLUME = "volume";
    private static final String ISSUE = "issue";
    private static final String PAGE = "page";
    private static final String APPROVAL_MODE = "approvalMode";
    private static final String WEBSITE = "website";
    private static final String TYPE = "type";
    private static final String COMMENT = "comment";

    private Map<String, String> props;

    public ReferenceImpl(final String author, final Integer year, final String title,
                         final String abstractText, final String journal, final String volume, final String issue,
                         final Integer page, final Integer approvalMode, final String website,
                         final ReferenceType referenceType, final String comment) {

        props = new HashMap<>(12);
        if (StringUtils.isNotEmpty(author)) {
            props.put(AUTHOR, author);
        }

        if (year != null) {
            props.put(YEAR, year.toString());
        }

        if (StringUtils.isNotEmpty(title)) {
            props.put(TITLE, title);
        }

        if (StringUtils.isNotEmpty(abstractText)) {
            props.put(ABSTRACT_TEXT, abstractText);
        }

        if (StringUtils.isNotEmpty(journal)) {
            props.put(JOURNAL, journal);
        }

        if (StringUtils.isNotEmpty(volume)) {
            props.put(VOLUME, volume);
        }

        if (StringUtils.isNotEmpty(issue)) {
            props.put(ISSUE, issue);
        }

        if (page != null) {
            props.put(PAGE, page.toString());
        }

        if (approvalMode != null) {
            props.put(APPROVAL_MODE, approvalMode.toString());
        }

        if (StringUtils.isNotEmpty(website)) {
            props.put(WEBSITE, website);
        }

        if (referenceType != null) {
            props.put(TYPE, referenceType.name());
        }

        if (StringUtils.isNotEmpty(comment)) {
            props.put(COMMENT, comment);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getAuthor() {
        return props.get(AUTHOR);
    }

    /**
     * {@inheritDoc}
     */
    public Integer getYear() {
        return props.containsKey(YEAR) ? Integer.parseInt(props.get(YEAR)) : null;
    }

    /**
     * {@inheritDoc}
     */
    public String getTitle() {
        return props.get(TITLE);
    }

    /**
     * {@inheritDoc}
     */
    public String getAbstractText() {
        return props.get(ABSTRACT_TEXT);
    }

    /**
     * {@inheritDoc}
     */
    public String getJournal() {
        return props.get(JOURNAL);
    }

    /**
     * {@inheritDoc}
     */
    public String getVolume() {
        return props.get(VOLUME);
    }

    /**
     * {@inheritDoc}
     */
    public String getIssue() {
        return props.get(ISSUE);
    }

    /**
     * {@inheritDoc}
     */
    public Integer getPage() {
        return props.containsKey(PAGE) ? Integer.parseInt(props.get(PAGE)) : null;
    }

    /**
     * {@inheritDoc}
     */
    public Integer getApprovalMode() {
        return props.containsKey(APPROVAL_MODE) ? Integer.parseInt(props.get(APPROVAL_MODE)) : null;
    }

    /**
     * {@inheritDoc}
     */
    public String getWebsite() {
        return props.get(WEBSITE);
    }

    /**
     * {@inheritDoc}
     */
    public ReferenceType getType() {
        return props.containsKey(TYPE) ? ReferenceType.valueOf(props.get(TYPE)) : null;
    }

    /**
     * {@inheritDoc}
     */
    public String getComment() {
        return props.get(COMMENT);
    }

    /**
     * {@inheritDoc}
     */
    public void setAuthor(final String author) {
        if (StringUtils.isNotEmpty(author)) {
            props.put(AUTHOR, author);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setYear(final int year) {
        props.put(YEAR, Integer.toString(year));
    }

    /**
     * {@inheritDoc}
     */
    public void setTitle(final String title) {
        props.put(TITLE, title);
    }

    /**
     * {@inheritDoc}
     */
    public void setAbstractText(final String abstractText) {
        if (StringUtils.isNotEmpty(abstractText)) {
            props.put(ABSTRACT_TEXT, abstractText);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setJournal(final String journal) {
        if (StringUtils.isNotEmpty(journal)) {
            props.put(JOURNAL, journal);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setVolume(final String volume) {
        if (StringUtils.isNotEmpty(volume)) {
            props.put(VOLUME, volume);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setIssue(final String issue) {
        if (StringUtils.isNotEmpty(issue)) {
            props.put(ISSUE, issue);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setPage(final int page) {
        props.put(PAGE, Integer.toString(page));
    }

    /**
     * {@inheritDoc}
     */
    public void setApprovalMode(final Integer approvalMode) {
        props.put(APPROVAL_MODE, approvalMode.toString());
    }

    /**
     * {@inheritDoc}
     */
    public void setWebsite(final String website) {
        if (StringUtils.isNotEmpty(website)) {
            props.put(WEBSITE, website);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setType(final ReferenceType type) {
        if (type != null) {
            props.put(TYPE, type.name());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setComment(final String comment) {
        if (StringUtils.isNotEmpty(comment)) {
            props.put(COMMENT, comment);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSetAuthor() {
        return props.containsKey(AUTHOR);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSetYear() {
        return props.containsKey(YEAR);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSetTitle() {
        return props.containsKey(TITLE);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSetAbstractText() {
        return props.containsKey(ABSTRACT_TEXT);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSetJournal() {
        return props.containsKey(JOURNAL);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSetVolume() {
        return props.containsKey(VOLUME);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSetIssue() {
        return props.containsKey(ISSUE);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSetPage() {
        return props.containsKey(PAGE);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSetApprovalMode() {
        return props.containsKey(APPROVAL_MODE);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSetWebsite() {
        return props.containsKey(WEBSITE);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSetType() {
        return props.containsKey(TYPE);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSetComment() {
        return props.containsKey(COMMENT);
    }

    public String toString() {
        return props.get(AUTHOR) + "_" + props.get(YEAR) + "_" + props.get(TITLE);
    }
}
