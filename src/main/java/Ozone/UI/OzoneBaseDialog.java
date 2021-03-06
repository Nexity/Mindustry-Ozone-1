/*******************************************************************************
 * Copyright 2021 Itzbenz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package Ozone.UI;

import mindustry.gen.Icon;

public abstract class OzoneBaseDialog extends OzoneDialog {
	public OzoneBaseDialog() {
		super();
	}
	
	public OzoneBaseDialog(String title, DialogStyle style) {
		super(title, style);
	}
	
	public OzoneBaseDialog(String title) {
		super(title);
	}
	
	@Override
	protected void ctor() {
		super.ctor();
		shown(this::setup);
		onResize(this::setup);
	}
	
	protected void addRefreshButton() {
		buttons.button("Refresh", Icon.refresh, this::setup).size(210f, 64f);
	}
	
	protected abstract void setup();
}
