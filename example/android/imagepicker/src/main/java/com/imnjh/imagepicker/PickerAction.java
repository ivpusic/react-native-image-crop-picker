package com.imnjh.imagepicker;

import java.util.ArrayList;

public interface PickerAction {
  void proceedResultAndFinish(ArrayList<String> selected, boolean original, int resultCode);
}