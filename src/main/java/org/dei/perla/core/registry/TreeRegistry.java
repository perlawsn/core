package org.dei.perla.core.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.Fpc;

public class TreeRegistry implements Registry {

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private volatile Node root = new Node(null);
	private Map<Integer, Fpc> fpcMap = new HashMap<>();

	@Override
	public Fpc get(int id) {
		lock.readLock().lock();
		try {
			return fpcMap.get(id);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Collection<Fpc> getAll() {
		lock.readLock().lock();
		try {
			return fpcMap.values();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Collection<Fpc> getByAttribute(Collection<Attribute> with,
			Collection<Attribute> without) {
		List<Attribute> withList = new ArrayList<>(with);
		Collections.sort(withList);
		List<Attribute> withoutList = new ArrayList<>(without);
		Collections.sort(withoutList);

		Collection<Fpc> result = new ArrayList<>();
		lock.readLock().lock();
		try {
			find(root, withList, 0, withoutList, 0, result);
		} finally {
			lock.readLock().unlock();
		}

		return result;
	}

	private void find(Node node, List<Attribute> withList, int withIdx,
			List<Attribute> withoutList, int withoutIdx, Collection<Fpc> result) {
		if (withIdx == withList.size() && withoutIdx == withoutList.size()) {
			return;
		}

		for (Node child : node.children) {

			if (withIdx < withList.size()) {
				Attribute withAtt = withList.get(withIdx);
				int with = child.id.compareTo(withAtt);
				if (with == 0) {
					withIdx += 1;
					if (withIdx == withList.size()) {
						result.addAll(child.fpcs);
					}
				}

				if (with <= 0) {
					find(child, withList, withIdx, withoutList, withoutIdx, result);
				} else {
					return;
				}
			}

			if (withoutIdx < withoutList.size()) {
				Attribute withoutAtt = withoutList.get(withoutIdx);
				boolean without = child.id.equals(withoutAtt);
				if (without) {
					withoutIdx += 1;
					result.removeAll(child.fpcs);
				}

				if (withIdx == withList.size() && withoutIdx < withoutList.size()) {
					find(child, withList, withIdx, withoutList, withoutIdx, result);
					continue;
				}
			}
		}
	}

	@Override
	public void add(Fpc fpc) {
		List<Attribute> attributeList = new LinkedList<>(fpc.getAttributes());
		Collections.sort(attributeList);

		lock.writeLock().lock();
		try {
			fpcMap.put(fpc.getId(), fpc);
			root = add(root, fpc, attributeList);
		} finally {
			lock.writeLock().unlock();
		}
	}

	private Node add(Node node, Fpc fpc, List<Attribute> attributeList) {
		if (node.fpcs.contains(fpc)) {
			return node;
		}

		Node clone = node.clone();
		clone.fpcs.add(fpc);

		// Search terminated, return
		if (attributeList.size() == 0) {
			return clone;
		}

		Attribute id = attributeList.get(0);
		Node child = node.getChild(id);
		// Create a new branch
		if (child == null) {
			child = createTree(fpc, attributeList);
			clone.children.add(child);
			return clone;

		}

		// Continue visiting the tree
		attributeList.remove(0);
		Node newChild = add(child, fpc, attributeList);
		clone.children.remove(child);
		clone.children.add(newChild);
		return clone;
	}

	private Node createTree(Fpc fpc, List<Attribute> attributeList) {
		Node root = new Node(attributeList.remove(0));
		root.fpcs.add(fpc);
		Node previous = root;

		for (Attribute att : attributeList) {
			Node node = new Node(att);
			node.fpcs.add(fpc);
			previous.children.add(node);
			previous = node;
		}

		return root;
	}

	@Override
	public void remove(Fpc fpc) {
		List<Attribute> attributeList = new LinkedList<>(fpc.getAttributes());
		Collections.sort(attributeList);

		lock.writeLock().lock();
		try {
			fpcMap.remove(fpc);
			root = remove(root, fpc, attributeList);
		} finally {
			lock.writeLock().unlock();
		}
	}

	private Node remove(Node node, Fpc fpc, List<Attribute> attributeList) {
		if (!node.fpcs.contains(fpc)) {
			return node;
		}

		Node clone = node.clone();
		clone.fpcs.remove(fpc);

		// Search terminated, return
		if (attributeList.size() == 0) {
			return clone;
		}

		Attribute id = attributeList.remove(0);
		Node child = node.getChild(id);
		// Return immediately if we're in the wrong branch
		if (child == null) {
			return clone;
		}

		// Detach the branch if it's empty
		if (child.fpcs.size() == 1 && child.fpcs.contains(fpc)) {
			clone.children.remove(child);
			return clone;
		}

		// Continue visiting the tree
		Node newChild = remove(child, fpc, attributeList);
		clone.children.remove(child);
		clone.children.add(newChild);

		return clone;
	}

	/**
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	public class Node implements Comparable<Node> {

		private final Attribute id;
		private final Collection<Fpc> fpcs = new HashSet<>();
		private final SortedSet<Node> children = new TreeSet<>();

		protected Node(Attribute id) {
			this.id = id;
		}

		public Node getChild(Attribute id) {
			for (Node child : children) {
				int comp = child.id.compareTo(id);
				if (comp == 0) {
					return child;
				} else if (comp > 0) {
					break;
				}
			}
			return null;
		}

		@Override
		public int compareTo(Node o) {
			return id.compareTo(o.id);
		}

		@Override
		public Node clone() {
			Node n = new Node(id);
			n.fpcs.addAll(fpcs);
			n.children.addAll(children);
			return n;
		}

	}

}
