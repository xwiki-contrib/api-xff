package org.xwiki.xff.core.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.xwiki.xff.core.XFFExplorer;

public class PackageExplorerIntegrationTests
{
    @Test
    public void fromRoot() throws IOException
    {
        Path xffPath = Paths.get("target/test-classes/package");
        
        XFFExplorer walker = new XFFExplorer(xffPath);
        assertTrue(walker.hasNext());
        assertEquals(Paths.get("wikis/templatewiki/wiki.xml"), walker.next());
        assertTrue(walker.hasNext());
        assertEquals(Paths.get("wikis/xwiki/wiki.xml"), walker.next());
        assertTrue(walker.hasNext());
        assertEquals(Paths.get("wikis/xwiki/spaces/MySpace/pages/MyPage1/page.xml"), walker.next());
        assertTrue(walker.hasNext());
        assertEquals(Paths.get("wikis/xwiki/spaces/MySpace/pages/MyPage2/page.xml"), walker.next());
        assertTrue(walker.hasNext());
        assertEquals(Paths.get("wikis/xwiki/spaces/Space/space.xml"), walker.next());
        assertTrue(walker.hasNext());
        assertEquals(Paths.get("wikis/xwiki/spaces/Space/pages/Page/page.xml"), walker.next());
        assertTrue(walker.hasNext());
        assertEquals(Paths.get("wikis/xwiki/spaces/Space/pages/Page/metadata/content.xwiki21"), walker.next());
        assertTrue(walker.hasNext());
        assertEquals(Paths.get("wikis/xwiki/spaces/Space/pages/Page/attachments/logo1.png"), walker.next());
        assertTrue(walker.hasNext());
        assertEquals(Paths.get("wikis/xwiki/spaces/Space/pages/Page/attachments/logo2.png"), walker.next());
        assertTrue(walker.hasNext());
        assertEquals(Paths.get("wikis/xwiki/spaces/Space/pages/Page/classes/class.xml"), walker.next());
        assertTrue(walker.hasNext());
        assertEquals(Paths.get("wikis/xwiki/spaces/Space/pages/Page/classes/properties/answer/customDisplay.xwiki21"), walker.next());
        assertTrue(walker.hasNext());
        assertEquals(Paths.get("wikis/xwiki/spaces/Space/pages/Page/objects/Space.Page/0/object.xml"), walker.next());
        assertTrue(walker.hasNext());
        assertEquals(Paths.get("wikis/xwiki/spaces/Space/pages/Page/objects/XWiki.StyleSheetExtension/0/object.xml"), walker.next());
        assertTrue(walker.hasNext());
        assertEquals(Paths.get("wikis/xwiki/spaces/Space/pages/Page/objects/XWiki.StyleSheetExtension/0/properties/code.css"), walker.next());
        assertTrue(walker.hasNext());
        assertEquals(Paths.get("wikis/xwiki/spaces/Space/spaces/SubSpace/pages/SubPage/page.xml"), walker.next());
        assertFalse(walker.hasNext());
        for (Path path : walker) {
            System.out.println(path.toString());
        }
    }
}
