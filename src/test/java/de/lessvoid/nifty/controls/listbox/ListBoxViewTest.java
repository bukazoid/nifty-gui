package de.lessvoid.nifty.controls.listbox;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ListBoxViewTest {
  private ListBoxImpl<TestItem> listBox = new ListBoxImpl<TestItem>();
  private TestItem o1 = new TestItem("o1");
  private TestItem o2 = new TestItem("o2");
  private TestItem o3 = new TestItem("o3");
  private ListBoxView<TestItem> view;

  @SuppressWarnings("unchecked")
  @Before
  public void before() {
    view = createMock(ListBoxView.class);
    assertEquals(0, listBox.bindToView(view, 2));
  }

  @After
  public void after() {
    verify(view);
  }

  @Test
  public void testUpdateViewWithEmptyList() {
    view.display(ListBoxTestTool.buildValues(null, null), -1, ListBoxTestTool.buildValuesSelection());
    replay(view);

    listBox.updateView(0);
  }

  @Test
  public void testSingleEntry() {
    view.updateTotalCount(1);
    view.display(ListBoxTestTool.buildValues(o1, null), 0, ListBoxTestTool.buildValuesSelection());
    replay(view);

    listBox.addItem(o1);
  }

  @Test
  public void testTwoEntries() {
    view.updateTotalCount(1);
    view.display(ListBoxTestTool.buildValues(o1, null), 0, ListBoxTestTool.buildValuesSelection());
    view.updateTotalCount(2);
    view.display(ListBoxTestTool.buildValues(o1, o2), 0, ListBoxTestTool.buildValuesSelection());
    replay(view);

    listBox.addItem(o1);
    listBox.addItem(o2);
  }

  @Test
  public void testThreeEntries() {
    view.updateTotalCount(1);
    view.display(ListBoxTestTool.buildValues(o1, null), 0, ListBoxTestTool.buildValuesSelection());
    view.updateTotalCount(2);
    view.display(ListBoxTestTool.buildValues(o1, o2), 0, ListBoxTestTool.buildValuesSelection());
    view.updateTotalCount(3);
    view.display(ListBoxTestTool.buildValues(o1, o2), 0, ListBoxTestTool.buildValuesSelection());
    replay(view);

    listBox.addItem(o1);
    listBox.addItem(o2);
    listBox.addItem(o3);
  }
}
