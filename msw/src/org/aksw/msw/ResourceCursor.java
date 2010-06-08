/**
 * 
 */
package org.aksw.msw;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

/**
 * @author natanael
 *
 */
public class ResourceCursor implements Cursor {

	/* (non-Javadoc)
	 * @see android.database.Cursor#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#copyStringToBuffer(int, android.database.CharArrayBuffer)
	 */
	@Override
	public void copyStringToBuffer(int arg0, CharArrayBuffer arg1) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#deactivate()
	 */
	@Override
	public void deactivate() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#getBlob(int)
	 */
	@Override
	public byte[] getBlob(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#getColumnIndex(java.lang.String)
	 */
	@Override
	public int getColumnIndex(String columnName) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#getColumnIndexOrThrow(java.lang.String)
	 */
	@Override
	public int getColumnIndexOrThrow(String columnName)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#getColumnName(int)
	 */
	@Override
	public String getColumnName(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#getColumnNames()
	 */
	@Override
	public String[] getColumnNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#getDouble(int)
	 */
	@Override
	public double getDouble(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#getExtras()
	 */
	@Override
	public Bundle getExtras() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#getFloat(int)
	 */
	@Override
	public float getFloat(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#getInt(int)
	 */
	@Override
	public int getInt(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#getLong(int)
	 */
	@Override
	public long getLong(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#getPosition()
	 */
	@Override
	public int getPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#getShort(int)
	 */
	@Override
	public short getShort(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#getString(int)
	 */
	@Override
	public String getString(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#getWantsAllOnMoveCalls()
	 */
	@Override
	public boolean getWantsAllOnMoveCalls() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#isAfterLast()
	 */
	@Override
	public boolean isAfterLast() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#isBeforeFirst()
	 */
	@Override
	public boolean isBeforeFirst() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#isClosed()
	 */
	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#isFirst()
	 */
	@Override
	public boolean isFirst() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#isLast()
	 */
	@Override
	public boolean isLast() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#isNull(int)
	 */
	@Override
	public boolean isNull(int columnIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#move(int)
	 */
	@Override
	public boolean move(int offset) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#moveToFirst()
	 */
	@Override
	public boolean moveToFirst() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#moveToLast()
	 */
	@Override
	public boolean moveToLast() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#moveToNext()
	 */
	@Override
	public boolean moveToNext() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#moveToPosition(int)
	 */
	@Override
	public boolean moveToPosition(int position) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#moveToPrevious()
	 */
	@Override
	public boolean moveToPrevious() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#registerContentObserver(android.database.ContentObserver)
	 */
	@Override
	public void registerContentObserver(ContentObserver observer) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#registerDataSetObserver(android.database.DataSetObserver)
	 */
	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#requery()
	 */
	@Override
	public boolean requery() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#respond(android.os.Bundle)
	 */
	@Override
	public Bundle respond(Bundle extras) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#setNotificationUri(android.content.ContentResolver, android.net.Uri)
	 */
	@Override
	public void setNotificationUri(ContentResolver cr, Uri uri) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#unregisterContentObserver(android.database.ContentObserver)
	 */
	@Override
	public void unregisterContentObserver(ContentObserver observer) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see android.database.Cursor#unregisterDataSetObserver(android.database.DataSetObserver)
	 */
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

}
