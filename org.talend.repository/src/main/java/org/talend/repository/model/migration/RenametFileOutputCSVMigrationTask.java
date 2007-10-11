// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2007 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.repository.model.migration;

import java.util.Arrays;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.AddPropertyCSVOptionConversion;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.conversions.RenameComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractMigrationTask;
import org.talend.core.model.migration.IProjectMigrationTask;

/**
 * Migration task use to rename component name tFileOutputCSV to tFileOutputDelimited.
 */
public class RenametFileOutputCSVMigrationTask extends AbstractMigrationTask implements IProjectMigrationTask {

    public ExecutionResult execute(Project project) {

        try {
            IComponentFilter filter1 = new NameComponentFilter("tFileOutputCSV"); //$NON-NLS-1$

            IComponentConversion addProperty = new AddPropertyCSVOptionConversion();
            IComponentConversion renameComponent = new RenameComponentConversion("tFileOutputDelimited");

            ModifyComponentsAction.searchAndModify(filter1, Arrays.<IComponentConversion> asList(addProperty,
                    renameComponent));

            return ExecutionResult.SUCCESS_WITH_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }
}
