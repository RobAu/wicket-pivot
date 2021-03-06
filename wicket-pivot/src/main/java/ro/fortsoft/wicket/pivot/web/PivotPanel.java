/*
 * Copyright 2012 Decebal Suiu
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.wicket.pivot.web;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.PropertyModel;

import ro.fortsoft.wicket.pivot.DefaultPivotModel;
import ro.fortsoft.wicket.pivot.PivotDataSource;
import ro.fortsoft.wicket.pivot.PivotField;
import ro.fortsoft.wicket.pivot.PivotModel;

/**
 * @author Decebal Suiu
 */
public class PivotPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private WebMarkupContainer areasContainer;
	private PivotModel pivotModel;
	private PivotTable pivotTable;
	private AjaxLink<Void> computeLink;
	private boolean autoCompute;

	public PivotPanel(String id, PivotDataSource pivotDataSource) {
		super(id);
		
		// create a pivot model
		pivotModel = createPivotModel(pivotDataSource);
				
		pivotModel.calculate();
		
		areasContainer = new WebMarkupContainer("areas");
		areasContainer.setOutputMarkupId(true);
		add(areasContainer);
		
		RepeatingView areaRepeater = new RepeatingView("area");
		areasContainer.add(areaRepeater);
		List<PivotField.Area> areas = PivotField.Area.getValues();
		for (PivotField.Area area : areas) {
			areaRepeater.add(new PivotAreaPanel(areaRepeater.newChildId(), area));
		}
		
		pivotTable = createPivotTabel("pivotTable", pivotModel);
		add(pivotTable);
		
		AjaxCheckBox showGrandTotalForColumnCheckBox = new AjaxCheckBox("showGrandTotalForColumn", new PropertyModel<Boolean>(this, "pivotModel.showGrandTotalForColumn")) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				// TODO Auto-generated method stub
			}
			
		};
		add(showGrandTotalForColumnCheckBox);

		AjaxCheckBox showGrandTotalForRowCheckBox = new AjaxCheckBox("showGrandTotalForRow", new PropertyModel<Boolean>(this, "pivotModel.showGrandTotalForRow")) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				// TODO Auto-generated method stub
			}
			
		};
		add(showGrandTotalForRowCheckBox);

		AjaxCheckBox autoComputeCheckBox = new AjaxCheckBox("autoCompute", new PropertyModel<Boolean>(this, "autoCompute")) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				computeLink.setVisible(!autoCompute);
				target.add(computeLink);
			}
			
		};
		add(autoComputeCheckBox);
		
		computeLink = new IndicatingAjaxLink<Void>("compute") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				compute(target);
			}

			/*
			@Override
			public boolean isEnabled() {
				return verify();
			}
			*/
			
		};
		computeLink.setOutputMarkupPlaceholderTag(true);
		computeLink.add(AttributeModifier.append("class", new AbstractReadOnlyModel<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				return verify() ? "btn-success" : "btn-success disabled";
			}
			
		}));
		add(computeLink);
		
		add(new PivotResourcesBehavior());
	}

	 @Override
	 public void onEvent(IEvent<?> event) {
		 super.onEvent(event);
			
         if (event.getPayload() instanceof AreaChangedEvent) {
        	 AjaxRequestTarget target = ((AreaChangedEvent) event.getPayload()).getAjaxRequestTarget();
        	 target.add(areasContainer);
        	 target.add(computeLink);
        	 
        	 if (autoCompute) {
        		 compute(target);
        	 }
         }
	}

	public PivotModel getPivotModel() {
		return pivotModel;
	}

	protected PivotModel createPivotModel(PivotDataSource pivotDataSource) {
		PivotModel pivotModel = new DefaultPivotModel(pivotDataSource);
		
		// debug
		/*
		Tree columnsHeaderTree =  pivotModel.getColumnsHeaderTree();
		System.out.println("### Columns Header Tree ###");
		TreeHelper.printTree(columnsHeaderTree.getRoot());
		TreeHelper.printLeafValues(columnsHeaderTree.getRoot());

		Tree rowsHeaderTree =  pivotModel.getRowsHeaderTree();
		System.out.println("### Rows Header Tree ### ");
		TreeHelper.printTree(rowsHeaderTree.getRoot());
		TreeHelper.printLeafValues(rowsHeaderTree.getRoot());
		*/

		return pivotModel;
	}

	protected PivotTable createPivotTabel(String id, PivotModel pivotModel) {
		PivotTable pivotTable = new PivotTable(id, pivotModel);
		pivotTable.setOutputMarkupPlaceholderTag(true);
		pivotTable.setVisible(false);
		
		return pivotTable;
	}

	private boolean verify() {
		return !pivotModel.getFields(PivotField.Area.DATA).isEmpty() && (!pivotModel.getFields(PivotField.Area.COLUMN).isEmpty() ||
				!pivotModel.getFields(PivotField.Area.ROW).isEmpty());
	}

	private void compute(AjaxRequestTarget target) {
		if (!verify()) {
			return;
		}
		
		pivotModel.calculate();
		PivotTable newPivotTable = new PivotTable("pivotTable", pivotModel);
		pivotTable.replaceWith(newPivotTable);
		pivotTable = newPivotTable;
		target.add(pivotTable);
	}
	
}
