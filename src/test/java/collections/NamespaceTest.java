package collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.useless.collections.NamespaceID;

public class NamespaceTest {
    @Test
    @DisplayName("Test Namespace 2String Constructor")
    void namespaceCreation1() {
        // Valid creation
        NamespaceID namespaceID_1 = new NamespaceID("minecraft", "value1");
        Assertions.assertEquals(namespaceID_1.namespace, "minecraft");
        Assertions.assertEquals(namespaceID_1.value, "value1");
        Assertions.assertEquals(namespaceID_1.toString(), "minecraft:value1");

        NamespaceID namespaceID_2 = new NamespaceID("minecraft", "value1");
        Assertions.assertEquals(namespaceID_2.namespace, "minecraft");
        Assertions.assertEquals(namespaceID_2.value, "value1");
        Assertions.assertEquals(namespaceID_2.toString(), "minecraft:value1");

        Assertions.assertEquals(namespaceID_1.namespace, namespaceID_2.namespace);
        Assertions.assertEquals(namespaceID_1.value, namespaceID_2.value);
        Assertions.assertEquals(namespaceID_1, namespaceID_2);
        Assertions.assertEquals(namespaceID_1.toString(), namespaceID_2.toString());

        NamespaceID namespaceID_3 = new NamespaceID("bta", "value2");
        Assertions.assertEquals(namespaceID_3.namespace, "bta");
        Assertions.assertEquals(namespaceID_3.value, "value2");
        Assertions.assertEquals(namespaceID_3.toString(), "bta:value2");

        Assertions.assertNotEquals(namespaceID_3.namespace, "minecraft");
        Assertions.assertNotEquals(namespaceID_3.value, "value1");
        Assertions.assertNotEquals(namespaceID_3.toString(), "minecraft:value1");

        Assertions.assertNotEquals(namespaceID_3, namespaceID_1);
        Assertions.assertNotEquals(namespaceID_3.value, namespaceID_1.value);
        Assertions.assertNotEquals(namespaceID_3.namespace, namespaceID_1.namespace);
        Assertions.assertNotEquals(namespaceID_3.toString(), namespaceID_1.toString());

        Assertions.assertNotEquals(namespaceID_3, namespaceID_2);
        Assertions.assertNotEquals(namespaceID_3.value, namespaceID_2.value);
        Assertions.assertNotEquals(namespaceID_3.namespace, namespaceID_2.namespace);
        Assertions.assertNotEquals(namespaceID_3.toString(), namespaceID_2.toString());

        // Invalid creation
        Assertions.assertThrowsExactly(NullPointerException.class, () -> new NamespaceID(null, null));
        Assertions.assertThrowsExactly(NullPointerException.class, () -> new NamespaceID("minecraft", null));
        Assertions.assertThrowsExactly(NullPointerException.class, () -> new NamespaceID(null, "value1"));

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new NamespaceID("minecraft:", "value1"));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new NamespaceID("minecraft", "value1:"));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new NamespaceID("minecraft:", "value1:"));
    }

    @Test
    @DisplayName("Test Namespace 1String Constructor")
    void namespaceCreation2() {
        String formatted_1 = "minecraft:value1";
        NamespaceID namespaceID_1 = new NamespaceID(formatted_1);
        Assertions.assertEquals(namespaceID_1.namespace, "minecraft");
        Assertions.assertEquals(namespaceID_1.value, "value1");
        Assertions.assertEquals(namespaceID_1.toString(), formatted_1);
        Assertions.assertEquals(namespaceID_1.namespace + ":" + namespaceID_1.value, formatted_1);

        NamespaceID namespaceID_2 = new NamespaceID(formatted_1);
        Assertions.assertEquals(namespaceID_2.namespace, "minecraft");
        Assertions.assertEquals(namespaceID_2.value, "value1");
        Assertions.assertEquals(namespaceID_2.toString(), formatted_1);
        Assertions.assertEquals(namespaceID_2.namespace + ":" + namespaceID_2.value, formatted_1);

        Assertions.assertEquals(namespaceID_1.namespace, namespaceID_2.namespace);
        Assertions.assertEquals(namespaceID_1.value, namespaceID_2.value);
        Assertions.assertEquals(namespaceID_1, namespaceID_2);
        Assertions.assertEquals(namespaceID_1.toString(), namespaceID_2.toString());

        Assertions.assertThrowsExactly(NullPointerException.class, () -> new NamespaceID(null));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new NamespaceID("minecraft::value1"));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new NamespaceID("minecraft:value1:"));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new NamespaceID(":value1"));
        Assertions.assertDoesNotThrow(() -> new NamespaceID("minecraft:value"));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new NamespaceID("minecraft value"));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> new NamespaceID("minecraft|value"));
    }
}
