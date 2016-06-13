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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Miguel Alba
 */
public class UncertaintiesImpl implements Uncertainties {

  private static final String ID = "id";
  private static final String MODEL_NAME = "modelName";
  private static final String COMMENT = "comment";
  private static final String R2 = "r2";
  private static final String RMS = "rms";
  private static final String SSE = "sse";
  private static final String AIC = "aic";
  private static final String BIC = "bic";
  private static final String DOF = "dof";

  private Map<String, String> props;

  /**
   * Creates a UncertaintiesImpl instance.
   * 
   * @param id: Ignored if null.
   * @param comment: Ignored if null or empty string.
   * @param r2: Ignored if null.
   * @param rms: Ignored if null.
   * @param sse: Ignored if null.
   * @param aic: Ignored if null.
   * @param bic: Ignored if null.
   * @param dof: Ignored if null.
   */
  public UncertaintiesImpl(final Integer id, final String modelName, final String comment,
      final Double r2, final Double rms, final Double sse, final Double aic, final Double bic,
      final Integer dof) {
    props = new HashMap<>(9);
    if (id != null)
      props.put(ID, id.toString());
    if (modelName != null && !modelName.isEmpty())
      props.put(MODEL_NAME, modelName);
    if (comment != null && !comment.isEmpty())
      props.put(COMMENT, comment);
    if (r2 != null)
      props.put(R2, r2.toString());
    if (rms != null)
      props.put(RMS, rms.toString());
    if (sse != null)
      props.put(SSE, sse.toString());
    if (aic != null)
      props.put(AIC, aic.toString());
    if (bic != null)
      props.put(BIC, bic.toString());
    if (dof != null)
      props.put(DOF, dof.toString());
  }

  /** {@inheritDoc} */
  public Integer getID() {
    return props.containsKey(ID) ? Integer.parseInt(props.get(ID)) : null;
  }

  /** {@inheritDoc} */
  public String getModelName() {
    return props.get(MODEL_NAME);
  }

  /** {@inheritDoc} */
  public String getComment() {
    return props.get(COMMENT);
  }

  /** {@inheritDoc} */
  public Double getR2() {
    return props.containsKey(R2) ? Double.parseDouble(props.get(R2)) : null;
  }

  /** {@inheritDoc} */
  public Double getRMS() {
    return props.containsKey(RMS) ? Double.parseDouble(props.get(RMS)) : null;
  }

  /** {@inheritDoc} */
  public Double getSSE() {
    return props.containsKey(SSE) ? Double.parseDouble(props.get(SSE)) : null;
  }

  /** {@inheritDoc} */
  public Double getAIC() {
    return props.containsKey(AIC) ? Double.parseDouble(props.get(AIC)) : null;
  }

  /** {@inheritDoc} */
  public Double getBIC() {
    return props.containsKey(BIC) ? Double.parseDouble(props.get(BIC)) : null;
  }

  /** {@inheritDoc} */
  public Integer getDOF() {
    return props.containsKey(DOF) ? Integer.parseInt(props.get(DOF)) : null;
  }

  /** {@inheritDoc} */
  public void setID(final int id) {
    props.put(ID, Integer.toString(id));
  }

  /** {@inheritDoc} */
  public void setModelName(final String modelName) {
    if (modelName != null && !modelName.isEmpty()) {
      props.put(MODEL_NAME, modelName);
    }
  }

  /** {@inheritDoc} */
  public void setComment(final String comment) {
    if (comment != null && !comment.isEmpty()) {
      props.put(COMMENT, comment);
    }
  }

  /** {@inheritDoc} */
  public void setR2(final double r2) {
    props.put(R2, Double.toString(r2));
  }

  /** {@inheritDoc} */
  public void setRMS(final double rms) {
    props.put(RMS, Double.toString(rms));
  }

  /** {@inheritDoc} */
  public void setSSE(final double sse) {
    props.put(SSE, Double.toString(sse));
  }

  /** {@inheritDoc} */
  public void setAIC(final double aic) {
    props.put(AIC, Double.toString(aic));
  }

  /** {@inheritDoc} */
  public void setBIC(final double bic) {
    props.put(BIC, Double.toString(bic));
  }

  /** {@inheritDoc} */
  public void setDOF(final int dof) {
    props.put(DOF, Integer.toString(dof));
  }

  /** {@inheritDoc} */
  public boolean isSetID() {
    return props.containsKey(ID);
  }

  /** {@inheritDoc} */
  public boolean isSetModelName() {
    return props.containsKey(MODEL_NAME);
  }

  /** {@inheritDoc} */
  public boolean isSetComment() {
    return props.containsKey(COMMENT);
  }

  /** {@inheritDoc} */
  public boolean isSetR2() {
    return props.containsKey(R2);
  }

  /** {@inheritDoc} */
  public boolean isSetRMS() {
    return props.containsKey(RMS);
  }

  /** {@inheritDoc} */
  public boolean isSetSSE() {
    return props.containsKey(SSE);
  }

  /** {@inheritDoc} */
  public boolean isSetAIC() {
    return props.containsKey(AIC);
  }

  /** {@inheritDoc} */
  public boolean isSetBIC() {
    return props.containsKey(BIC);
  }

  /** {@inheritDoc} */
  public boolean isSetDOF() {
    return props.containsKey(DOF);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((props == null) ? 0 : props.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    UncertaintiesImpl other = (UncertaintiesImpl) obj;
    if (props == null) {
      if (other.props != null)
        return false;
    } else if (!props.equals(other.props))
      return false;
    return true;
  }
}
